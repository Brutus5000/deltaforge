package net.brutus5000.deltaforge.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.brutus5000.deltaforge.client.api.ApiClient;
import net.brutus5000.deltaforge.client.error.CheckoutException;
import net.brutus5000.deltaforge.client.error.InitializeException;
import net.brutus5000.deltaforge.client.io.DownloadService;
import net.brutus5000.deltaforge.client.model.Patch;
import net.brutus5000.deltaforge.client.model.Repository;
import net.brutus5000.deltaforge.client.model.Tag;
import net.brutus5000.deltaforge.client.model.TagType;
import net.brutus5000.deltaforge.client.patching.PatchGraph;
import net.brutus5000.deltaforge.client.patching.PatchGraphFactory;
import net.brutus5000.deltaforge.client.patching.PatchService;
import net.brutus5000.deltaforge.patching.io.IoService;
import net.brutus5000.deltaforge.patching.io.ValidationService;
import net.brutus5000.deltaforge.patching.meta.patch.PatchMetadata;
import net.brutus5000.deltaforge.patching.meta.patch.PatchRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;

@Service
@Slf4j
public class RepositoryService {
    private final DeltaforgeClientProperties properties;
    private final IoService ioService;
    private final ObjectMapper objectMapper;
    private final ValidationService validationService;
    private final ApiClient apiClient;
    private final DownloadService downloadService;
    private final PatchService patchService;
    private final PatchGraphFactory patchGraphFactory;
    private final Map<Repository, RepositoryCacheItem> repositoryCache = new HashMap<>();

    public RepositoryService(DeltaforgeClientProperties properties, IoService ioService,
                             ObjectMapper objectMapper, ValidationService validationService, ApiClient apiClient,
                             DownloadService downloadService, PatchService patchService,
                             PatchGraphFactory patchGraphFactory) {
        this.properties = properties;
        this.ioService = ioService;
        this.objectMapper = objectMapper;
        this.validationService = validationService;
        this.apiClient = apiClient;
        this.downloadService = downloadService;
        this.patchService = patchService;
        this.patchGraphFactory = patchGraphFactory;
    }

    private Optional<Repository> fromCache(String repositoryName) {
        return repositoryCache.keySet().stream()
                .filter(repository -> Objects.equals(repositoryName, repository.getName()))
                .findFirst();
    }

    private void afterLoad(Repository repository) {
        RepositoryCacheItem cacheItem = new RepositoryCacheItem()
                .setPatchGraph(patchGraphFactory.buildPatchGraph(repository));

        repositoryCache.put(repository, cacheItem);

        repository.setMainDirectory(Paths.get(properties.getRootDirectory(), repository.getName()));
        repository.setUrl(properties.getServerContentUrl() + "/" + repository.getName());
    }

    public Optional<Repository> findByName(String name) throws IOException {
        Optional<Repository> cacheResult = fromCache(name);

        if (cacheResult.isPresent()) {
            return cacheResult;
        }

        Path infoPath = Path.of(properties.getRootDirectory(), name, Repository.DELTAFORGE_INFO_FILE);
        if (!ioService.isFile(infoPath)) {
            return Optional.empty();
        }

        Repository repository = objectMapper.readValue(infoPath.toFile(), Repository.class);
        afterLoad(repository);

        return Optional.of(repository);
    }

    public Repository initialize(String name, Path repositoryPath, Path sourceFolder) throws InitializeException {
        Optional<Repository> cacheResult = fromCache(name);
        if (cacheResult.isPresent()) {
            throw new InitializeException("Repository is already cached: " + name);
        }

        if (ioService.isDirectory(repositoryPath)) {
            throw new InitializeException("Cannot initialize into existing directory: " + repositoryPath);
        }

        try {
            Repository repository = apiClient.getRepository(name)
                    .orElseThrow(() -> new InitializeException("Repository not found on the server."))
                    .setMainDirectory(repositoryPath);

            Tag sourceTag = identifySourceTag(repository, sourceFolder)
                    .orElseThrow(() -> new InitializeException("The given source folder does not match any source tag."));

            repository.setCurrentTag(sourceTag.getName());

            log.debug("Copy from sourceFiles `{}` to repositoryPath `{}`", sourceFolder, repositoryPath);
            ioService.copyDirectory(sourceFolder, repository.getCurrentTagFolder());
            objectMapper.writeValue(repository.getInfoPath().toFile(), repository);
            afterLoad(repository);

            return repository;
        } catch (IOException e) {
            log.error("Initialization failed, removing created directory `{}`", repositoryPath);
            ioService.deleteQuietly(repositoryPath);
            throw new InitializeException("An error occurred during writing into the repository directory.", e);
        }
    }

    public void refreshTagGraph(Repository repository) throws IOException {
        log.debug("Reload remote repository: {}", repository);
        Repository freshRepository = apiClient.getRepository(repository.getName())
                .orElseThrow(() -> new IOException("Repository not found on the server."));

        repository.setPatchGraph(freshRepository.getPatchGraph());
        repositoryCache.get(repository).patchGraph.refreshGraph();
    }

    public Optional<Tag> identifySourceTag(Repository repository, Path sourceFolder) {
        return repository.getTags().stream()
                .filter(tag -> tag.getType() == TagType.SOURCE)
                .filter(tag -> validationService.validateChecksums(sourceFolder, tag.getValidateMetadata()))
                .findFirst();
    }

    public List<Patch> calculatePatchPath(Repository repository, String tagName) throws CheckoutException {
        Tag currentTag = repository.findTagByName(repository.getCurrentTag())
                .orElseThrow(() -> new CheckoutException("Unknown tag: " + repository.getCurrentTag()));
        Tag targetTag = repository.findTagByName(tagName)
                .orElseThrow(() -> new CheckoutException("Unknown tag: " + tagName));

        PatchGraph patchGraph = repositoryCache.get(repository).getPatchGraph();

        return patchGraph.getPatchPath(currentTag, targetTag);
    }

    public void downloadPatchIfMissing(Repository localRepository, Patch patch) throws InterruptedException, IOException, URISyntaxException {
        Path archiveDestination = getPatchFilePath(localRepository, patch, "zip");
        Path metadataDestination = getPatchFilePath(localRepository, patch, "json");

        ioService.createDirectories(archiveDestination.getParent());

        if (ioService.isFile(archiveDestination)) {
            log.debug("Patch file {} is already on disk.", archiveDestination.getFileName());
        } else {
            URL downloadURL = localRepository.getRemotePatchURL(patch.getFrom().getName(), patch.getTo().getName(), "zip");
            log.debug("Downloading patch file from: {}", downloadURL);
            downloadService.download(downloadURL, archiveDestination);

        }

        if (ioService.isFile(metadataDestination)) {
            log.debug("Patch metadata file {} is already on disk.", metadataDestination.getFileName());
        } else {
            URL downloadMetadataURL = localRepository.getRemotePatchURL(patch.getFrom().getName(), patch.getTo().getName(), "json");
            log.debug("Downloading patch metadata file from: {}", downloadMetadataURL);
            downloadService.download(downloadMetadataURL, metadataDestination);
        }
    }

    private Path getPatchFilePath(Repository repository, Patch patch, String fileExtension) {
        return repository.getPatchPath(patch.getFrom().getName(), patch.getTo().getName(), fileExtension);
    }

    public void applyPatch(Repository repository, Patch patch) {
        log.debug("Applying patch {} on repository {}", patch, repository);
        try {
            Path patchDirectory = ioService.createTempDirectory("deltaforge_patch_");
            Path resultDirectory = ioService.createTempDirectory("deltaforge_patchresult_");
            Path baselineDirectory = repository.getInitialBaselineFolder();

            PatchRequest patchRequest = (PatchRequest) new PatchRequest()
                    .setSourceFolder(repository.getMainDirectory().resolve(Repository.DELTAFORGE_CURRENT_TAG_FOLDER))
                    .setPatchFolder(patchDirectory)
                    .setInitialBaselineFolder(baselineDirectory)
                    .setTargetFolder(resultDirectory)
                    .setRepository(patch.getRepository().getName())
                    .setFromTag(patch.getFrom().getName())
                    .setToTag(patch.getTo().getName())
                    .setItems(patch.getMetadata().getItems())
                    .setProtocol(patch.getMetadata().getProtocol())
                    .setFileRenaming(patch.getMetadata().getFileRenaming());

            log.debug("Unzip patch file to: {}", patchDirectory);
            ioService.unzip(getPatchFilePath(repository, patch, "zip"), patchDirectory);

            log.debug("Invoking patchService with PatchRequest: {}", patchRequest);
            patchService.applyPatch(patchRequest);

            log.debug("Setting current tag to: {}", patch.getTo().getName());
            repository.setCurrentTag(patch.getTo().getName());
        } catch (IOException e) {
            String message = MessageFormat.format("Applying patch failed: {0}", patch);
            throw new CheckoutException(message, e);
        }
    }

    public void loadPatchMetadata(Repository repository, Patch patch) throws IOException {
        PatchMetadata metadata = objectMapper.readValue(getPatchFilePath(repository, patch, "json").toFile(), PatchMetadata.class);
        patch.setMetadata(metadata);
    }

    @Data
    static class RepositoryCacheItem {
        private PatchGraph patchGraph;
    }
}

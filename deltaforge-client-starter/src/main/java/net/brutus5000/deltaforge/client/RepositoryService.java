package net.brutus5000.deltaforge.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.brutus5000.deltaforge.client.api.ApiClient;
import net.brutus5000.deltaforge.client.error.CheckoutException;
import net.brutus5000.deltaforge.client.error.InitializeException;
import net.brutus5000.deltaforge.client.io.DownloadService;
import net.brutus5000.deltaforge.client.model.Patch;
import net.brutus5000.deltaforge.client.model.Repository;
import net.brutus5000.deltaforge.client.model.Tag;
import net.brutus5000.deltaforge.client.model.TagType;
import net.brutus5000.deltaforge.patching.io.IoService;
import net.brutus5000.deltaforge.patching.io.ValidationService;
import net.brutus5000.deltaforge.patching.meta.patch.PatchRequest;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.io.GraphMLImporter;
import org.jgrapht.io.ImportException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    public RepositoryService(DeltaforgeClientProperties properties, IoService ioService,
                             ObjectMapper objectMapper, ValidationService validationService, ApiClient apiClient, DownloadService downloadService, PatchService patchService) {
        this.properties = properties;
        this.ioService = ioService;
        this.objectMapper = objectMapper;
        this.validationService = validationService;
        this.apiClient = apiClient;
        this.downloadService = downloadService;
        this.patchService = patchService;
    }

    public Optional<Repository> findByName(String name) throws IOException {
        Path infoPath = Path.of(properties.getRootDirectory(), name, Repository.DELTAFORGE_INFO_FILE);
        if (!ioService.isFile(infoPath)) {
            return Optional.empty();
        }

        return Optional.of(objectMapper.readValue(infoPath.toFile(), Repository.class));
    }

    public Repository initialize(String name, Path repositoryPath, Path sourceFolder) throws InitializeException {

        if (ioService.isDirectory(repositoryPath)) {
            throw new InitializeException("Cannot initialize into existing directory: " + repositoryPath);
        }

        try {
            Repository repository = apiClient.getRepository(name)
                    .setMainDirectory(repositoryPath);

            Tag sourceTag = identifySourceTag(repository, sourceFolder)
                    .orElseThrow(() -> new InitializeException("The given source folder does not match any source tag."));

            repository.setCurrentTag(sourceTag.getName());

            log.debug("Copy from sourceFiles `{}` to repositoryPath `{}`", sourceFolder, repositoryPath);
            ioService.copyDirectory(sourceFolder, repository.getCurrentTagFolder());
            objectMapper.writeValue(repository.getInfoPath().toFile(), repository);

            return repository;
        } catch (IOException e) {
            log.error("Initialization failed, removing created directory `{}`", repositoryPath);
            ioService.deleteQuietly(repositoryPath);
            throw new InitializeException("An error occurred during writing into the repository directory.", e);
        }
    }

    public void refreshTagGraph(Repository repository) throws IOException {
        log.debug("Reload remote repository: {}", repository);
        Repository freshRepository = apiClient.getRepository(repository.getName());

        repository
                .setGraph(freshRepository.getGraph());
    }

    public Optional<Tag> identifySourceTag(Repository repository, Path sourceFolder) {
        return repository.getTags().stream()
                .filter(tag -> tag.getType() == TagType.SOURCE)
                .filter(tag -> validationService.validateChecksums(sourceFolder, tag.getValidateMetadata()))
                .findFirst();
    }

    // TODO: Test
    public List<Patch> getPatchPath(Repository repository, String tagName) throws ImportException {
        GraphMLImporter<Tag, Patch> importer = new GraphMLImporter<>(
                (id, attributes) -> repository.getTags().stream()
                        .filter(tag -> Objects.equals(id, tag.getName()))
                        .findFirst()
                        .orElseThrow(() -> new CheckoutException("Unknown tag with id: " + id)),
                (from, to, label, attributes) -> repository.getPatches().stream()
                        .filter(patch -> Objects.equals(patch.getFrom(), from) &&
                                Objects.equals(patch.getTo(), to))
                        .findFirst()
                        .orElseThrow(() -> new CheckoutException(MessageFormat.format("No patch found from {0} to {1}", from.getName(), to.getName())))
        );

        Graph<Tag, Patch> graph = new DirectedMultigraph<>(Patch.class);
        importer.importGraph(graph, new StringReader(repository.getGraph()));

        Tag currentTag = repository.findTagByName(repository.getCurrentTag())
                .orElseThrow(() -> new CheckoutException("Unknown tag: " + repository.getCurrentTag()));
        Tag targetTag = repository.findTagByName(tagName)
                .orElseThrow(() -> new CheckoutException("Unknown tag: " + tagName));

        GraphPath<Tag, Patch> graphPath = DijkstraShortestPath.findPathBetween(graph, currentTag, targetTag);

        return graphPath.getEdgeList();
    }

    public void downloadPatchIfMissing(Repository localRepository, Patch patch) throws InterruptedException, IOException, URISyntaxException {
        Path archiveDestination = getPatchFilePath(localRepository, patch, "zip");

        if (ioService.isFile(archiveDestination)) {
            log.debug("Patch file {} is already on disk.", archiveDestination.getFileName());
        } else {
            URL downloadURL = localRepository.getRemotePatchURL(patch.getFrom().getName(), patch.getTo().getName());
            log.debug("Downloading patch file from: {}", downloadURL);
            downloadService.download(downloadURL, archiveDestination);
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
                    .setSourceFolder(repository.getMainDirectory())
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
            log.error("Applying patch failed: {}", e.getMessage(), e);
        }
    }
}

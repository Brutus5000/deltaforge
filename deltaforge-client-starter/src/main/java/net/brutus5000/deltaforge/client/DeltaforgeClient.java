package net.brutus5000.deltaforge.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.brutus5000.deltaforge.client.error.CheckoutException;
import net.brutus5000.deltaforge.client.error.InitializeException;
import net.brutus5000.deltaforge.client.io.DownloadService;
import net.brutus5000.deltaforge.client.model.Repository;
import net.brutus5000.deltaforge.client.model.Tag;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class DeltaforgeClient {
    private final DeltaforgeClientProperties properties;
    private final ObjectMapper objectMapper;
    private final DownloadService downloadService;
    private final RepositoryService repositoryService;

    public DeltaforgeClient(DeltaforgeClientProperties properties, ObjectMapper objectMapper, DownloadService downloadService, RepositoryService repositoryService) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.downloadService = downloadService;
        this.repositoryService = repositoryService;
    }

    public void checkoutTag(@NotNull Repository repository, @NotNull String tagName) throws CheckoutException {
        log.info("Checking out tag `{}` from repository `{}`", tagName, repository);

        if (Objects.equals(repository.getCurrentTag(), tagName)) {
            log.debug("Tag `{}` from repository `{}` is already checked out", tagName, repository);
            return;
        }

        repositoryService.calculatePatchPath(repository, tagName)
                .forEach(patch -> {
                    try {
                        log.info("Downloading patch `{}`", patch);
                        repositoryService.downloadPatchIfMissing(repository, patch);

                        log.info("Applying patch `{}`", patch);
                        repositoryService.applyPatch(repository, patch);
                    } catch (InterruptedException | IOException | URISyntaxException e) {
                        throw new CheckoutException("Could not download one or more patch files: " + e.getMessage(), e);
                    }
                });

    }

    public void checkoutLatest(@NotNull Repository repository, @NotNull String branch) throws CheckoutException {
        log.info("Checking out latest tag of branch `{}` from repository `{}`", branch, repository);

        try {
            repositoryService.refreshTagGraph(repository);
        } catch (IOException e) {
            throw new CheckoutException("Refreshing of tag patchGraph failed: " + e.getMessage(), e);
        }

        Tag latestTag = repository.getLatestTag(branch)
                .orElseThrow(() -> new CheckoutException(
                        MessageFormat.format("Latest tag for branch `{0}` in repository `{1}` not found.",
                                branch, repository)));

        checkoutTag(repository, latestTag.getName());
    }

    public Repository addRepository(@NotNull String name, @NotNull Path sourceFiles) throws InitializeException {
        return repositoryService.initialize(name, Paths.get(properties.getRootDirectory(), name), sourceFiles);
    }

    public Optional<Repository> loadRepository(String name) throws IOException {
        Optional<Repository> repositoryOptional = repositoryService.findByName(name);

        repositoryOptional.ifPresent(repository -> {
            // try to update data from remote repository
            try {
                repositoryService.refreshTagGraph(repository);
            } catch (IOException e) {
                log.warn("Updating localRepository `{}` from remote failed: {}", repository, e);
            }
        });

        return repositoryOptional;
    }
}

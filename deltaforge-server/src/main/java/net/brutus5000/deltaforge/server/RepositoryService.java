package net.brutus5000.deltaforge.server;

import net.brutus5000.deltaforge.server.model.Repository;
import net.brutus5000.deltaforge.server.repository.RepositoryRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RepositoryService {
    private final RepositoryRepository repositoryRepository;

    public RepositoryService(RepositoryRepository repositoryRepository) {
        this.repositoryRepository = repositoryRepository;
    }

    public Page<Repository> getRepositories(Pageable pageable) {
        return repositoryRepository.findAll(pageable);
    }

    public Optional<Repository> findByName(@NotNull String name) {
        return repositoryRepository.findByName(name);
    }

    public Repository addRepository(@NotNull String name) {
        Repository repository = new Repository()
                .setName(name);

        repositoryRepository.save(repository);

        return repository;
    }
//
//    Tag createTagFromFile(@NotNull UUID repositoryId, @NotNull String name, @NotNull String tagFolderPath);
//
//    Tag createTagFromGitTag(@NotNull UUID repositoryId, @NotNull String name, @NotNull String gitTagName);
//
//    Tag createTagFromGitCommit(@NotNull UUID repositoryId, @NotNull String name, @NotNull String gitCommitId);
//
//    List<Branch> getBranches(@NotNull UUID repositoryId);
//
//    Branch addBranch(@NotNull UUID repositoryId, @NotNull String name, @NotNull UUID initialBaselineTagId, @Nullable String gitBranch);
//
//    Tag addTagToBranch(@NotNull Branch branch, @NotNull Tag tag, @NotNull TagType type);
//
//    Patch[] findPatchPath(@NotNull Tag from, @NotNull Tag to);
}

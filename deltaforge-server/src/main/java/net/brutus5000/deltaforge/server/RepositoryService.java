package net.brutus5000.deltaforge.server;

import net.brutus5000.deltaforge.server.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface RepositoryService {
    List<Repository> getRepositories();

    Repository addRepository(@NotNull String name, @Nullable String gitUrl);

    Tag createTagFromFile(@NotNull UUID repositoryId, @NotNull String name, @NotNull String tagFolderPath);

    Tag createTagFromGitTag(@NotNull UUID repositoryId, @NotNull String name, @NotNull String gitTagName);

    Tag createTagFromGitCommit(@NotNull UUID repositoryId, @NotNull String name, @NotNull String gitCommitId);

    List<Branch> getBranches(@NotNull UUID repositoryId);

    Branch addBranch(@NotNull UUID repositoryId, @NotNull String name, @NotNull UUID initialBaselineTagId,
                     @Nullable String gitBranch);

    Tag addTagToBranch(@NotNull Branch branch, @NotNull Tag tag, @NotNull TagType type);

    Patch[] findPatchPath(@NotNull Tag from, @NotNull Tag to);
}

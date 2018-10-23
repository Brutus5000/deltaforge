package net.brutus5000.deltaforge;

import net.brutus5000.deltaforge.config.DeltaForgeProperties;
import net.brutus5000.deltaforge.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.UUID;

@SpringBootApplication
@EnableConfigurationProperties({DeltaForgeProperties.class})
public class DeltaForgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeltaForgeApplication.class, args);
    }

    @Bean
    public RepositoryService repositoryService() {
        return new RepositoryService() {
            @Override
            public List<Repository> getRepositories() {
                return null;
            }

            @Override
            public Repository addRepository(@NotNull String name, @Nullable String gitUrl) {
                return null;
            }

            @Override
            public Tag createTagFromFile(@NotNull UUID repositoryId, @NotNull String name, @NotNull String tagFolderPath) {
                return null;
            }

            @Override
            public Tag createTagFromGitTag(@NotNull UUID repositoryId, @NotNull String name, @NotNull String gitTagName) {
                return null;
            }

            @Override
            public Tag createTagFromGitCommit(@NotNull UUID repositoryId, @NotNull String name, @NotNull String gitCommitId) {
                return null;
            }

            @Override
            public List<Branch> getBranches(@NotNull UUID repositoryId) {
                return null;
            }

            @Override
            public Branch addBranch(@NotNull UUID repositoryId, @NotNull String name, @NotNull UUID initialBaselineTagId, @Nullable String gitBranch) {
                return null;
            }

            @Override
            public Tag addTagToBranch(@NotNull Branch branch, @NotNull Tag tag, @NotNull TagType type) {
                return null;
            }

            @Override
            public Patch[] findPatchPath(@NotNull Tag from, @NotNull Tag to) {
                return new Patch[0];
            }
        };
    }
}

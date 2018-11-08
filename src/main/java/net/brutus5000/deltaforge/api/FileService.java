package net.brutus5000.deltaforge.api;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.brutus5000.deltaforge.config.DeltaForgeProperties;
import net.brutus5000.deltaforge.error.ApiException;
import net.brutus5000.deltaforge.error.ErrorCode;
import net.brutus5000.deltaforge.model.*;
import net.brutus5000.deltaforge.repository.BranchRepository;
import net.brutus5000.deltaforge.repository.PatchTaskRepository;
import net.brutus5000.deltaforge.repository.TagAssignmentRepository;
import net.brutus5000.deltaforge.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
public class FileService {
    private final static String TAGS_FOLDER = "tags";

    private final DeltaForgeProperties properties;
    private final RepoService repoService;
    private final BranchRepository branchRepository;
    private final TagRepository tagRepository;
    private final TagAssignmentRepository tagAssignmentRepository;
    private final PatchTaskRepository patchTaskRepository;

    public FileService(DeltaForgeProperties properties, RepoService repoService, BranchRepository branchRepository, TagRepository tagRepository, TagAssignmentRepository tagAssignmentRepository, PatchTaskRepository patchTaskRepository) {
        this.properties = properties;
        this.repoService = repoService;
        this.branchRepository = branchRepository;
        this.tagRepository = tagRepository;
        this.tagAssignmentRepository = tagAssignmentRepository;
        this.patchTaskRepository = patchTaskRepository;
    }

    public boolean existsTagFolderPath(@NonNull Tag tag) {
        if (tag.getRepository() == null || tag.getRepository().getName() == null)
            return false;

        if (tag.getName() == null)
            return false;

        return Files.exists(buildTagFolderPath(tag));
    }

    public Path buildTagFolderPath(@NonNull Tag tag) {
        Assert.notNull(tag.getRepository(), "Tag must have a repository");
        Assert.notNull(tag.getName(), "Tag must have a name");
        return Paths.get(properties.getRootRepositoryPath(), tag.getRepository().getName(), TAGS_FOLDER, tag.getName());
    }

    public void createBranch(@NonNull UUID repositoryId, @NonNull String name, @NonNull UUID baseTagId) {
        Repository repository = repoService.findById(repositoryId);

        Tag baseTag = tagRepository.findById(baseTagId)
                .orElseThrow(() -> ApiException.of(ErrorCode.TAG_NOT_FOUND, baseTagId));

        if (StringUtils.isEmpty(name)) {
            throw ApiException.of(ErrorCode.STRING_IS_EMPTY, name);
        }

        branchRepository.findByRepositoryAndName(repository, name)
                .ifPresent(branch -> {
                    throw ApiException.of(ErrorCode.BRANCH_NAME_IN_USE, branch.getName());
                });

        Branch newBranch = new Branch()
                .setRepository(repository)
                .setName(name)
                .setInitialBaseline(baseTag)
                .setCurrentBaseline(baseTag)
                .setCurrentTag(baseTag);

        log.info("Creating new branch: {}", newBranch);
        branchRepository.save(newBranch);

        TagAssignment tagAssignment = new TagAssignment()
                .setBranch(newBranch)
                .setTag(baseTag);

        log.debug("Creating tag assignment for new branch id '{}': {}", newBranch.getId(), tagAssignment);
        tagAssignmentRepository.save(tagAssignment);
    }

    public void addSourceTagToBranch(@NonNull UUID branchId, @NonNull UUID sourceTagId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> ApiException.of(ErrorCode.BRANCH_NOT_FOUND, branchId));

        Tag sourceTag = tagRepository.findById(sourceTagId)
                .orElseThrow(() -> ApiException.of(ErrorCode.TAG_NOT_FOUND, sourceTagId));

        TagAssignment tagAssignment = new TagAssignment()
                .setBranch(branch)
                .setTag(sourceTag);

        log.debug("Creating tag assignment for branch id '{}': {}", branch.getId(), tagAssignment);
        tagAssignmentRepository.save(tagAssignment);

        PatchTask patchTask = new PatchTask()
                .setStatus(TaskStatus.PENDING)
                .setFrom(sourceTag)
                .setTo(branch.getInitialBaseline());

        log.debug("Creating patch task: {}", patchTask);
        patchTaskRepository.save(patchTask);
    }
}

package net.brutus5000.deltaforge.api;

import com.google.common.annotations.VisibleForTesting;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.brutus5000.deltaforge.config.DeltaForgeProperties;
import net.brutus5000.deltaforge.error.ApiException;
import net.brutus5000.deltaforge.error.ErrorCode;
import net.brutus5000.deltaforge.events.PatchCreatedEvent;
import net.brutus5000.deltaforge.events.TagCreatedEvent;
import net.brutus5000.deltaforge.model.*;
import net.brutus5000.deltaforge.repository.*;
import net.brutus5000.deltaforge.resthandler.ValidationBuilder;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.BidirectionalDijkstraShortestPath;
import org.jgrapht.graph.DirectedWeightedPseudograph;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static net.brutus5000.deltaforge.resthandler.ValidationBuilder.whenNotNull;

@Service
@Slf4j
public class RepoService {
    private final DeltaForgeProperties properties;
    private final RepositoryRepository repositoryRepository;
    private final BranchRepository branchRepository;
    private final PatchTaskRepository patchTaskRepository;
    private final TagRepository tagRepository;
    private final TagAssignmentRepository tagAssignmentRepository;
    private final PatchRepository patchRepository;
    private final FileService fileService;

    public RepoService(DeltaForgeProperties properties, RepositoryRepository repositoryRepository, BranchRepository branchRepository, PatchTaskRepository patchTaskRepository, TagRepository tagRepository, TagAssignmentRepository tagAssignmentRepository, PatchRepository patchRepository, FileService fileService) {
        this.properties = properties;
        this.repositoryRepository = repositoryRepository;
        this.branchRepository = branchRepository;
        this.patchTaskRepository = patchTaskRepository;
        this.tagRepository = tagRepository;
        this.tagAssignmentRepository = tagAssignmentRepository;
        this.patchRepository = patchRepository;
        this.fileService = fileService;
    }

    @VisibleForTesting
    public void validateCreate(@NonNull RepositoryCreate repositoryCreate) {
        new ValidationBuilder()
                .assertNotBlank(repositoryCreate.getName(), "name")
                .assertNotExists(
                        repositoryRepository::findByName, repositoryCreate.getName(),
                        ErrorCode.REPOSITORY_NAME_IN_USE, repositoryCreate.getName())
                .assertNotNull(repositoryCreate.getInitialBaseline(), "initialBaseline")
                .assertThat(fileService::existsTagFolderPath, repositoryCreate, ErrorCode.TAG_FOLDER_NOT_EXISTS)
                .conditionalAssertNotExists(
                        whenNotNull(repositoryCreate.getGitUrl()),
                        repositoryRepository::findByGitUrl, repositoryCreate.getGitUrl(),
                        ErrorCode.REPOSITORY_GIT_URL_IN_USE, repositoryCreate.getName())
                .validate();
    }

    public Repository createRepository(@NonNull RepositoryCreate repositoryCreate) {
        validateCreate(repositoryCreate);

        Repository repository = new Repository()
                .setName(repositoryCreate.getName())
                .setGitUrl(repositoryCreate.getGitUrl());

        repositoryRepository.saveAndFlush(repository);

        Tag initialBaselineTag = new Tag()
                .setName(repositoryCreate.getInitialBaseline())
                .setRepository(repository)
                .setType(TagType.BASELINE);

        tagRepository.saveAndFlush(initialBaselineTag);

        repository.setInitialBaseline(initialBaselineTag);
        repositoryRepository.save(repository);

        return repository;
    }

    @Transactional
    public Graph<Tag, Patch> buildGraph(@NonNull Repository repository) {
        log.debug("Building graph for repository: {}", repository);

        final Graph<Tag, Patch> graph = new DirectedWeightedPseudograph<>(Patch.class);
        final Set<Tag> tags = tagRepository.findAllByRepository(repository);

        tags.forEach(graph::addVertex);
        patchRepository.findAllByFromIn(tags).forEach(patch -> graph.addEdge(patch.getFrom(), patch.getTo(), patch));
        graph.edgeSet().forEach(
                patch -> graph.setEdgeWeight(patch, patch.getFileSize())
        );

        return graph;
    }

    private void enqueuePatch(@NonNull Tag initialBaseline, @NonNull Tag from, @NonNull Tag to, boolean baselineCheck) {
        PatchTask currentToBaselinePatchTask = new PatchTask()
                .setStatus(TaskStatus.PENDING)
                .setInitialBaseline(initialBaseline)
                .setFrom(from)
                .setTo(to)
                .setBaselineCheck(baselineCheck);

        log.debug("Creating patch task from tag {} to tag {}", from, to);
        patchTaskRepository.save(currentToBaselinePatchTask);
    }

    @Transactional
    public void addTagToBranch(@NonNull UUID branchId, @NonNull UUID tagId, @NonNull TagType tagType) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> ApiException.of(ErrorCode.BRANCH_NOT_FOUND, branchId));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> ApiException.of(ErrorCode.TAG_NOT_FOUND, tagId.toString()));

        Optional<TagAssignment> existingAssignment = tagAssignmentRepository.findByBranchAndTag(branch, tag);
        if (existingAssignment.isPresent()) {
            throw ApiException.of(ErrorCode.TAG_ALREADY_ASSIGNED, tag.getId().toString(), branch.getId());
        }

        TagAssignment tagAssignment = new TagAssignment()
                .setBranch(branch)
                .setTag(tag);

        log.debug("Creating tag assignment for branch id '{}': {}", branchId, tagAssignment);
        tagAssignmentRepository.save(tagAssignment);

        Tag initialBaselineTag = branch.getRepository().getInitialBaseline();

        if (tagType == TagType.SOURCE) {
            enqueuePatch(initialBaselineTag, branch.getCurrentBaseline(), tag, false);
            enqueuePatch(initialBaselineTag, tag, branch.getCurrentBaseline(), false);
        } else {
            enqueuePatch(initialBaselineTag, branch.getCurrentTag(), tag, true);
            enqueuePatch(initialBaselineTag, tag, branch.getCurrentTag(), false);
        }

        log.debug("Updating branch '{}' current tag to: {}", branch, tag);
        branch.setCurrentTag(tag);
        branchRepository.save(branch);
    }

    @EventListener
    public void onPatchCreated(@NonNull PatchCreatedEvent event) {
        log.debug("Adding patch to repository graph for PatchCreatedEvent: {}", event);

        final Patch patch = event.getPatch();
        final Repository repository = patch.getFrom().getRepository();

        repository.getPatchGraph().addEdge(patch.getFrom(), patch.getTo(), patch);
        repository.getPatchGraph().setEdgeWeight(patch, patch.getFileSize());

        if (event.isBaselineCheck()) {
            final BidirectionalDijkstraShortestPath<Tag, Patch> shortestPathAlgorithm = new BidirectionalDijkstraShortestPath<>(repository.getPatchGraph());
            checkForBaselinePromotion(shortestPathAlgorithm, patch);
        } else {
            log.debug("Baseline check not requested for patch: {}", event.getPatch());
        }
    }

    private void checkForBaselinePromotion(@NonNull BidirectionalDijkstraShortestPath<Tag, Patch> shortestPathAlgorithm, @NonNull Patch patch) {
        Set<Branch> affectedBranches = branchRepository.findAllByCurrentTag(patch.getTo());

        for (Branch branch : affectedBranches) {
            GraphPath<Tag, Patch> path = shortestPathAlgorithm.getPath(branch.getCurrentBaseline(), patch.getTo());

            if (path.getWeight() > properties.getBaselineFilesizeThreshold()) {
                log.debug("Patch path filesize '{}' exceeds initialBaseline threshold of '{}'.", path.getWeight(), properties.getBaselineFilesizeThreshold());
                upgradeTagToBaseline(patch.getTo());
            } else {
                log.debug("Patch path filesize '{}' below initialBaseline threshold of '{}'.", path.getWeight(), properties.getBaselineFilesizeThreshold());
            }
        }
    }

    @Transactional
    public void upgradeTagToBaseline(@NonNull Tag tag) {
        log.info("Upgrading tag to initialBaseline: '{}'", tag);
        tag.setType(TagType.BASELINE);
        tagRepository.save(tag);

        Tag initialBaselineTag = tag.getRepository().getInitialBaseline();
        Set<Tag> baselineTags = tagRepository.findAllByRepositoryAndType(tag.getRepository(), TagType.BASELINE);
        for (Tag baseline : baselineTags) {
            log.debug("Enqueuing patches initialBaseline '{}' <-> new tag '{}'");
            enqueuePatch(initialBaselineTag, baseline, tag, false);
            enqueuePatch(initialBaselineTag, tag, baseline, false);
        }

        Set<Branch> patchedBranches = branchRepository.findAllByCurrentTag(tag);
        for (Branch branch : patchedBranches) {
            branch.setCurrentBaseline(tag);
        }

        branchRepository.saveAll(patchedBranches);
    }

    @EventListener
    public void onTagCreated(@NonNull TagCreatedEvent event) {
        log.debug("Adding tag to repository graph for TagCreatedEvent: {}", event);

        final Tag tag = event.getTag();
        tag.getRepository().getPatchGraph().addVertex(tag);
    }
}
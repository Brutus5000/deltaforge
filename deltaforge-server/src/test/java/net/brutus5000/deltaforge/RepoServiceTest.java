package net.brutus5000.deltaforge;

import net.brutus5000.deltaforge.api.RepoService;
import net.brutus5000.deltaforge.config.DeltaforgeServerProperties;
import net.brutus5000.deltaforge.events.PatchCreatedEvent;
import net.brutus5000.deltaforge.events.TagCreatedEvent;
import net.brutus5000.deltaforge.model.*;
import net.brutus5000.deltaforge.repository.*;
import org.jgrapht.Graph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepoServiceTest {
    @Mock
    private DeltaforgeServerProperties properties;
    @Mock
    private RepositoryRepository repositoryRepository;
    @Mock
    private BranchRepository branchRepository;
    @Mock
    private PatchTaskRepository patchTaskRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private TagAssignmentRepository tagAssignmentRepository;
    @Mock
    private PatchRepository patchRepository;

    @InjectMocks
    RepoService repoService;

    private Repository repository;
    private Branch branch;
    private Tag tag;
    @Mock
    private Graph<Tag, Patch> repositoryGraph;

    @BeforeEach
    void beforeEach() {
        repository = new Repository()
                .setId(UUID.randomUUID())
                .setPatchGraph(repositoryGraph)
                .setInitialBaseline(new Tag().setName("initialBaseline"));

        branch = new Branch()
                .setId(UUID.randomUUID())
                .setRepository(repository);

        tag = new Tag()
                .setId(UUID.randomUUID())
                .setRepository(repository);
    }

    @Nested
    class TestEventListener {
        @Test
        void onTagCreated() {

            repoService.onTagCreated(new TagCreatedEvent(tag));

            verify(repositoryGraph).addVertex(tag);
        }

        @Test
        void onPatchCreatedWithoutBaselineCheck() {
            Tag from = tag;
            Tag to = new Tag()
                    .setRepository(repository);
            long fileSize = 99L;
            Patch patch = new Patch()
                    .setFrom(from)
                    .setTo(to)
                    .setFileSize(fileSize);

            repoService.onPatchCreated(new PatchCreatedEvent(patch, false));

            verify(repositoryGraph).addEdge(from, to, patch);
            verify(repositoryGraph).setEdgeWeight(patch, fileSize);
        }

        @Test
        void upgradeTagToBaseline() {
            Tag newTag = new Tag()
                    .setId(UUID.randomUUID())
                    .setRepository(repository);

            when(tagRepository.findAllByRepositoryAndType(repository, TagType.BASELINE)).thenReturn(Set.of(tag));
            when(branchRepository.findAllByCurrentTag(newTag)).thenReturn(Set.of(branch));

            repoService.upgradeTagToBaseline(newTag);

            verify(patchTaskRepository, times(2)).save(any());
            final ArgumentCaptor<Set<Branch>> captor = ArgumentCaptor.forClass(Set.class);
            verify(branchRepository).saveAll(captor.capture());

            Set<Branch> allSaved = captor.getValue();
            assertThat(allSaved.size(), is(1));
            assertThat(allSaved, contains(branch));
            assertThat(branch.getCurrentBaseline(), is(newTag));
        }
    }
}

package net.brutus5000.deltaforge.server;

import net.brutus5000.deltaforge.server.api.RepoService;
import net.brutus5000.deltaforge.server.config.DeltaforgeServerProperties;
import net.brutus5000.deltaforge.server.events.PatchCreatedEvent;
import net.brutus5000.deltaforge.server.events.TagCreatedEvent;
import net.brutus5000.deltaforge.server.model.*;
import net.brutus5000.deltaforge.server.repository.*;
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
    private ChannelRepository channelRepository;
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
    private Channel channel;
    private Tag tag;
    @Mock
    private PatchGraph repositoryGraph;

    @BeforeEach
    void beforeEach() {
        repository = new Repository()
                .setId(UUID.randomUUID())
                .setPatchGraph(repositoryGraph)
                .setInitialBaseline(new Tag().setName("initialBaseline"));

        channel = new Channel()
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

            verify(repositoryGraph).addEdge(patch);
            verify(repositoryGraph).setEdgeWeight(patch, fileSize);
        }

        @Test
        void upgradeTagToBaseline() {
            Tag newTag = new Tag()
                    .setId(UUID.randomUUID())
                    .setRepository(repository);

            when(tagRepository.findAllByRepositoryAndType(repository, TagType.BASELINE)).thenReturn(Set.of(tag));
            when(channelRepository.findAllByCurrentTag(newTag)).thenReturn(Set.of(channel));

            repoService.upgradeTagToBaseline(newTag);

            verify(patchTaskRepository, times(2)).save(any());
            final ArgumentCaptor<Set<Channel>> captor = ArgumentCaptor.forClass(Set.class);
            verify(channelRepository).saveAll(captor.capture());

            Set<Channel> allSaved = captor.getValue();
            assertThat(allSaved.size(), is(1));
            assertThat(allSaved, contains(channel));
            assertThat(channel.getCurrentBaseline(), is(newTag));
        }
    }
}

package net.brutus5000.deltaforge.server.api;

import net.brutus5000.deltaforge.api.dto.TagTypeDto;
import net.brutus5000.deltaforge.server.config.DeltaforgeServerProperties;
import net.brutus5000.deltaforge.server.error.ApiException;
import net.brutus5000.deltaforge.server.error.ErrorCode;
import net.brutus5000.deltaforge.server.events.PatchCreatedEvent;
import net.brutus5000.deltaforge.server.events.TagCreatedEvent;
import net.brutus5000.deltaforge.server.model.*;
import net.brutus5000.deltaforge.server.repository.*;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static net.brutus5000.deltaforge.server.error.ApiExceptionWithCode.apiExceptionWithCodeAndArgs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    @Mock
    private ApiDtoMapper apiDtoMapper;

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

    @AfterEach
    void afterEach() {
        verifyNoMoreInteractions(properties, repositoryRepository, channelRepository, patchRepository, tagRepository,
                tagAssignmentRepository, patchRepository);
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

            verify(tagRepository).save(newTag);
            verify(tagRepository).findAllByRepositoryAndType(repository, TagType.BASELINE);
            verify(patchTaskRepository, times(2)).save(any());
            final ArgumentCaptor<Set<Channel>> captor = ArgumentCaptor.forClass(Set.class);
            verify(channelRepository).saveAll(captor.capture());

            Set<Channel> allSaved = captor.getValue();
            assertThat(allSaved.size(), is(1));
            assertThat(allSaved, contains(channel));
            assertThat(channel.getCurrentBaseline(), is(newTag));
        }
    }

    @Nested
    class TestAddTag {
        @Nested
        class ValidationErrors {

            private final String TAG_TYPE_NAME_INTERMEDIATE = TagTypeDto.INTERMEDIATE.name();

            @Test
            void UnknownChannel() {
                ApiException result = assertThrows(ApiException.class,
                        () -> repoService.addTagToChannel(channel.getId(), tag.getId(), TAG_TYPE_NAME_INTERMEDIATE));

                assertThat(result, is(apiExceptionWithCodeAndArgs(ErrorCode.CHANNEL_NOT_FOUND, channel.getId())));

                verify(channelRepository).findById(channel.getId());
            }

            @Test
            void NoInitialBaseline() {
                doReturn(Optional.of(channel)).when(channelRepository).findById(channel.getId());
                repository.setInitialBaseline(null);

                ApiException result = assertThrows(ApiException.class,
                        () -> repoService.addTagToChannel(channel.getId(), tag.getId(), TAG_TYPE_NAME_INTERMEDIATE));

                assertThat(result, is(apiExceptionWithCodeAndArgs(ErrorCode.REPOSITORY_BASELINE_MISSING, repository.getName())));

                verify(channelRepository).findById(channel.getId());
            }

            @Test
            void UnknownTag() {
                doReturn(Optional.of(channel)).when(channelRepository).findById(channel.getId());

                ApiException result = assertThrows(ApiException.class,
                        () -> repoService.addTagToChannel(channel.getId(), tag.getId(), TAG_TYPE_NAME_INTERMEDIATE));

                assertThat(result, is(apiExceptionWithCodeAndArgs(ErrorCode.TAG_NOT_FOUND, tag.getId())));

                verify(channelRepository).findById(channel.getId());
                verify(tagRepository).findById(tag.getId());
            }

            @Test
            void UnknownTagType() {
                doReturn(Optional.of(channel)).when(channelRepository).findById(channel.getId());
                doReturn(Optional.of(tag)).when(tagRepository).findById(tag.getId());

                String invalidTagType = "invalidTagType";

                ApiException result = assertThrows(ApiException.class,
                        () -> repoService.addTagToChannel(channel.getId(), tag.getId(), invalidTagType));

                assertThat(result, is(apiExceptionWithCodeAndArgs(ErrorCode.TAG_INVALID_TYPE, invalidTagType)));

                verify(channelRepository).findById(channel.getId());
                verify(tagRepository).findById(tag.getId());
            }

            @Test
            void TagAlreadyAssigned() {
                doReturn(Optional.of(channel)).when(channelRepository).findById(channel.getId());
                doReturn(Optional.of(tag)).when(tagRepository).findById(tag.getId());
                doReturn(TagType.INTERMEDIATE).when(apiDtoMapper).map(TagTypeDto.INTERMEDIATE);
                doReturn(Optional.of(mock(TagAssignment.class))).when(tagAssignmentRepository).findByChannelAndTag(channel, tag);

                ApiException result = assertThrows(ApiException.class,
                        () -> repoService.addTagToChannel(channel.getId(), tag.getId(), TAG_TYPE_NAME_INTERMEDIATE));

                assertThat(result, is(apiExceptionWithCodeAndArgs(ErrorCode.TAG_ALREADY_ASSIGNED, tag.getId(), channel.getId())));

                verify(channelRepository).findById(channel.getId());
                verify(tagRepository).findById(tag.getId());
                verify(tagAssignmentRepository).findByChannelAndTag(channel, tag);
            }
        }

        @Nested
        class Success {
            private Matcher<TagAssignment> matchingAssignment(Channel channel, Tag tag) {
                return new CustomTypeSafeMatcher<>("TagAssignment has set correct channel and tag") {
                    @Override
                    protected boolean matchesSafely(TagAssignment item) {
                        return Objects.equals(item.getChannel(), channel) && Objects.equals(item.getTag(), tag);
                    }
                };
            }

            @Test
            void addIntermediateTag() {
                String tagTypeDtoName = TagTypeDto.INTERMEDIATE.name();
                channel.setCurrentTag(new Tag()
                        .setId(UUID.randomUUID()));

                doReturn(Optional.of(channel)).when(channelRepository).findById(channel.getId());
                doReturn(Optional.of(tag)).when(tagRepository).findById(tag.getId());
                doReturn(TagType.INTERMEDIATE).when(apiDtoMapper).map(TagTypeDto.INTERMEDIATE);
                doReturn(Optional.empty()).when(tagAssignmentRepository).findByChannelAndTag(channel, tag);

                repoService.addTagToChannel(channel.getId(), tag.getId(), tagTypeDtoName);


                verify(channelRepository).findById(channel.getId());
                verify(tagRepository).findById(tag.getId());
                verify(tagAssignmentRepository).findByChannelAndTag(channel, tag);

                ArgumentCaptor<TagAssignment> tagAssignmentArgumentCaptor = ArgumentCaptor.forClass(TagAssignment.class);
                verify(tagAssignmentRepository).save(tagAssignmentArgumentCaptor.capture());
                assertThat(tagAssignmentArgumentCaptor.getValue(), is(matchingAssignment(channel, tag)));

                verify(patchTaskRepository, times(2)).save(any());

                verify(channelRepository).save(channel);
            }

            @Test
            void addSourceTag() {
                String tagTypeDtoName = TagTypeDto.SOURCE.name();
                channel.setCurrentBaseline(new Tag()
                        .setId(UUID.randomUUID()));

                doReturn(Optional.of(channel)).when(channelRepository).findById(channel.getId());
                doReturn(Optional.of(tag)).when(tagRepository).findById(tag.getId());
                doReturn(TagType.SOURCE).when(apiDtoMapper).map(TagTypeDto.SOURCE);
                doReturn(Optional.empty()).when(tagAssignmentRepository).findByChannelAndTag(channel, tag);

                repoService.addTagToChannel(channel.getId(), tag.getId(), tagTypeDtoName);


                verify(channelRepository).findById(channel.getId());
                verify(tagRepository).findById(tag.getId());
                verify(tagAssignmentRepository).findByChannelAndTag(channel, tag);

                ArgumentCaptor<TagAssignment> tagAssignmentArgumentCaptor = ArgumentCaptor.forClass(TagAssignment.class);
                verify(tagAssignmentRepository).save(tagAssignmentArgumentCaptor.capture());
                assertThat(tagAssignmentArgumentCaptor.getValue(), is(matchingAssignment(channel, tag)));

                verify(patchTaskRepository).save(any());

                verify(channelRepository).save(channel);
            }
        }
    }
}

package net.brutus5000.deltaforge;

import net.brutus5000.deltaforge.api.FileService;
import net.brutus5000.deltaforge.error.ApiException;
import net.brutus5000.deltaforge.error.ErrorCode;
import net.brutus5000.deltaforge.model.Repository;
import net.brutus5000.deltaforge.model.Tag;
import net.brutus5000.deltaforge.model.TagType;
import net.brutus5000.deltaforge.repository.TagRepository;
import net.brutus5000.deltaforge.resthandler.TagEventHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;
import java.util.Optional;

import static net.brutus5000.deltaforge.error.ApiExceptionWithMultipleCodes.apiExceptionWithCode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagEventHandlerTest {
    static final String TAG_NAME = "testTag";

    private Repository repository = new Repository();

    @Mock
    private TagRepository tagRepository;
    @Mock
    private EntityManager entityManager;
    @Mock
    private FileService fileService;

    @InjectMocks
    private TagEventHandler tagEventHandler;

    @BeforeEach
    void beforeEach() throws Exception {
    }

    @Nested
    class GivenNoExistingTags {
        @Nested
        class WhenCreating {
            @BeforeEach
            void beforeEach() throws Exception {
                //when(fileService.existsTagFolderPath(any(Tag.class))).thenReturn(true);
            }

            @Test
            void thenHandleBeforeCreateShouldFailOnEmptyTag() {
                Tag tag = new Tag();
                when(tagRepository.findByRepositoryAndName(null, null)).thenReturn(Optional.empty());
                when(fileService.existsTagFolderPath(any(Tag.class))).thenReturn(false);

                ApiException exception = assertThrows(ApiException.class, () -> tagEventHandler.handleBeforeCreate(tag));

                assertThat(exception, apiExceptionWithCode(
                        ErrorCode.PROPERTY_IS_NULL,
                        ErrorCode.PROPERTY_IS_NULL,
                        ErrorCode.PROPERTY_IS_NULL,
                        ErrorCode.TAG_FOLDER_NOT_EXISTS
                ));
            }

            @Test
            void thenHandleBeforeCreateShouldFailOnEmptyNameTag() {
                when(tagRepository.findByRepositoryAndName(repository, "")).thenReturn(Optional.empty());
                Tag tag = new Tag()
                        .setRepository(repository)
                        .setType(TagType.SOURCE)

                        .setName("");

                ApiException exception = assertThrows(ApiException.class, () -> tagEventHandler.handleBeforeCreate(tag));

                assertThat(exception, apiExceptionWithCode(ErrorCode.STRING_IS_EMPTY, ErrorCode.TAG_FOLDER_NOT_EXISTS));
            }

            @Test
            void thenHandleBeforeCreateShouldPassOnValidRepository() {
                when(tagRepository.findByRepositoryAndName(repository, TAG_NAME)).thenReturn(Optional.empty());
                when(fileService.existsTagFolderPath(any(Tag.class))).thenReturn(true);

                Tag tag = new Tag()
                        .setRepository(repository)
                        .setName(TAG_NAME)
                        .setType(TagType.SOURCE);

                tagEventHandler.handleBeforeCreate(tag);
            }
        }
    }

    @Nested
    class GivenExistingRepositories {
        @Nested
        class WhenCreating {
            private Tag tag;

            @BeforeEach
            void beforeEach() throws Exception {
                when(fileService.existsTagFolderPath(any(Tag.class))).thenReturn(true);

                tag = new Tag()
                        .setRepository(repository)
                        .setName(TAG_NAME)
                        .setType(TagType.SOURCE);
            }

            @Test
            void thenHandleBeforeCreateShouldFailOnNameAlreadyInUse() {
                when(tagRepository.findByRepositoryAndName(repository, TAG_NAME)).thenReturn(Optional.of(mock(Tag.class)));

                ApiException exception = assertThrows(ApiException.class, () -> tagEventHandler.handleBeforeCreate(tag));

                assertThat(exception, apiExceptionWithCode(ErrorCode.TAG_NAME_IN_USE));
            }
        }

        @Nested
        class WhenUpdating {
            String NEW_NAME = "new name";
            String NEW_GIT_URL = "http://localhost:8080";

            Tag preUpdate = new Tag()
                    .setName(TAG_NAME)
                    .setRepository(repository)
                    .setType(TagType.SOURCE);

            Tag tag = new Tag()
                    .setName(TAG_NAME)
                    .setRepository(repository)
                    .setType(TagType.SOURCE);

            @Test
            void thenHandleBeforeSaveShouldFailOnNameAlreadyInUse() {
                tag.setName(NEW_NAME);

                when(tagRepository.findById(any())).thenReturn(Optional.of(preUpdate));
                when(tagRepository.findByRepositoryAndName(repository, NEW_NAME)).thenReturn(Optional.of(mock(Tag.class)));

                ApiException exception = assertThrows(ApiException.class, () -> tagEventHandler.handleBeforeSave(tag));

                assertThat(exception, apiExceptionWithCode(ErrorCode.TAG_NAME_IN_USE));
            }

            @Test
            void thenHandleBeforeSaveShouldFailOnChangingRepository() {
                tag.setRepository(mock(Repository.class));

                when(tagRepository.findById(any())).thenReturn(Optional.of(preUpdate));

                ApiException exception = assertThrows(ApiException.class, () -> tagEventHandler.handleBeforeSave(tag));

                assertThat(exception, apiExceptionWithCode(ErrorCode.REPOSITORY_FIXED));
            }

            @Test
            void thenHandleBeforeSaveShouldPass() {
                tag
                        .setName(NEW_NAME);

                when(tagRepository.findById(any())).thenReturn(Optional.of(preUpdate));
                when(tagRepository.findByRepositoryAndName(repository, NEW_NAME)).thenReturn(Optional.empty());

                tagEventHandler.handleBeforeSave(tag);
            }
        }
    }
}

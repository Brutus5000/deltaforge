package net.brutus5000.deltaforge;

import net.brutus5000.deltaforge.error.ApiException;
import net.brutus5000.deltaforge.error.ErrorCode;
import net.brutus5000.deltaforge.model.Repository;
import net.brutus5000.deltaforge.model.Tag;
import net.brutus5000.deltaforge.repository.RepositoryRepository;
import net.brutus5000.deltaforge.resthandler.RepositoryEventHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;
import java.util.Optional;
import java.util.UUID;

import static net.brutus5000.deltaforge.error.ApiExceptionWithMultipleCodes.apiExceptionWithCode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryEventHandlerTest {
    static final String REPOSITORY_NAME = "testRepository";
    static final String REPOSITORY_GIT_URL = "http://localhost";

    @Mock
    private RepositoryRepository repositoryRepository;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private RepositoryEventHandler repositoryEventHandler;

    @BeforeEach
    void beforeEach() {
    }

    @Nested
    class GivenNoExistingRepositories {
        @Nested
        class WhenCreating {
//            @Test
//            void thenHandleBeforeCreateShouldFailOnEmptyRepository() {
//                when(repositoryRepository.findByName(null)).thenReturn(Optional.empty());
//                Repository repository = new Repository();
//
//                ApiException exception = assertThrows(ApiException.class, () -> repositoryEventHandler.handleBeforeCreate(repository));
//
//                assertThat(exception, apiExceptionWithCode(ErrorCode.PROPERTY_IS_NULL));
//            }
//
//            @Test
//            void thenHandleBeforeCreateShouldFailOnEmptyNameRepository() {
//                when(repositoryRepository.findByName("")).thenReturn(Optional.empty());
//                Repository repository = new Repository()
//                        .setName("");
//
//                ApiException exception = assertThrows(ApiException.class, () -> repositoryEventHandler.handleBeforeCreate(repository));
//
//                assertThat(exception, apiExceptionWithCode(ErrorCode.STRING_IS_EMPTY));
//            }
//
//            @Test
//            void thenHandleBeforeCreateShouldPassOnValidRepository() {
//                when(repositoryRepository.findByName(REPOSITORY_NAME)).thenReturn(Optional.empty());
//                Repository repository = new Repository()
//                        .setName(REPOSITORY_NAME);
//
//                repositoryEventHandler.handleBeforeCreate(repository);
//            }
        }
    }

    @Nested
    class GivenExistingRepositories {
//        @Nested
//        class WhenCreating {
//            Repository repository = new Repository()
//                    .setName(REPOSITORY_NAME)
//                    .setGitUrl(REPOSITORY_GIT_URL);
//
//            @Test
//            void thenHandleBeforeCreateShouldFailOnNameAlreadyInUse() {
//                when(repositoryRepository.findByName(REPOSITORY_NAME)).thenReturn(Optional.of(mock(Repository.class)));
//                when(repositoryRepository.findByGitUrl(REPOSITORY_GIT_URL)).thenReturn(Optional.empty());
//
//                ApiException exception = assertThrows(ApiException.class, () -> repositoryEventHandler.handleBeforeCreate(repository));
//
//                assertThat(exception, apiExceptionWithCode(ErrorCode.REPOSITORY_NAME_IN_USE));
//            }
//
//            @Test
//            void thenHandleBeforeCreateShouldFailOnGitUrlAlreadyInUse() {
//                when(repositoryRepository.findByName(REPOSITORY_NAME)).thenReturn(Optional.empty());
//                when(repositoryRepository.findByGitUrl(REPOSITORY_GIT_URL)).thenReturn(Optional.of(mock(Repository.class)));
//
//                ApiException exception = assertThrows(ApiException.class, () -> repositoryEventHandler.handleBeforeCreate(repository));
//
//                assertThat(exception, apiExceptionWithCode(ErrorCode.REPOSITORY_GIT_URL_IN_USE));
//            }
//        }

        @Nested
        class WhenUpdating {
            String NEW_NAME = "new name";
            String NEW_GIT_URL = "http://localhost:8080";

            Repository preUpdate = new Repository()
                    .setName(REPOSITORY_NAME)
                    .setGitUrl(REPOSITORY_GIT_URL)
                    .setInitialBaseline(new Tag()
                            .setName("initialTag")
                    );

            Repository repository = new Repository()
                    .setName(REPOSITORY_NAME)
                    .setGitUrl(REPOSITORY_GIT_URL)
                    .setInitialBaseline(new Tag()
                            .setName("initialTag")
                    );

            @Test
            void thenHandleBeforeSaveShouldFailOnNameAlreadyInUse() {
                repository.setName(NEW_NAME);

                when(repositoryRepository.findById(any())).thenReturn(Optional.of(preUpdate));
                when(repositoryRepository.findByName(NEW_NAME)).thenReturn(Optional.of(mock(Repository.class)));

                ApiException exception = assertThrows(ApiException.class, () -> repositoryEventHandler.handleBeforeSave(repository));

                assertThat(exception, apiExceptionWithCode(ErrorCode.REPOSITORY_NAME_IN_USE));
            }

            @Test
            void thenHandleBeforeSaveShouldFailOnGitUrlAlreadyInUse() {
                repository.setGitUrl(NEW_GIT_URL);

                when(repositoryRepository.findById(any())).thenReturn(Optional.of(preUpdate));
                when(repositoryRepository.findByGitUrl(NEW_GIT_URL)).thenReturn(Optional.of(mock(Repository.class)));

                ApiException exception = assertThrows(ApiException.class, () -> repositoryEventHandler.handleBeforeSave(repository));

                assertThat(exception, apiExceptionWithCode(ErrorCode.REPOSITORY_GIT_URL_IN_USE));
            }

            @Test
            void thenHandleBeforeSaveShouldFailOnChangingInitialBaseline() {
                repository.setInitialBaseline(new Tag()
                        .setId(UUID.randomUUID())
                        .setName("someNewTag"));

                when(repositoryRepository.findById(any())).thenReturn(Optional.of(preUpdate));

                ApiException exception = assertThrows(ApiException.class, () -> repositoryEventHandler.handleBeforeSave(repository));

                assertThat(exception, apiExceptionWithCode(ErrorCode.REPOSITORY_BASELINE_FIXED));
            }

            @Test
            void thenHandleBeforeSaveShouldPass() {
                repository
                        .setName(NEW_NAME)
                        .setGitUrl(NEW_GIT_URL);

                when(repositoryRepository.findById(any())).thenReturn(Optional.of(preUpdate));
                when(repositoryRepository.findByName(NEW_NAME)).thenReturn(Optional.empty());
                when(repositoryRepository.findByGitUrl(NEW_GIT_URL)).thenReturn(Optional.empty());

                repositoryEventHandler.handleBeforeSave(repository);
            }
        }
    }
}

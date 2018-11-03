package net.brutus5000.deltaforge;

import net.brutus5000.deltaforge.error.ApiException;
import net.brutus5000.deltaforge.error.ErrorCode;
import net.brutus5000.deltaforge.model.Branch;
import net.brutus5000.deltaforge.model.Repository;
import net.brutus5000.deltaforge.model.Tag;
import net.brutus5000.deltaforge.repository.BranchRepository;
import net.brutus5000.deltaforge.resthandler.BranchEventHandler;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchEventHandlerTest {
    static final String BRANCH_NAME = "testBranch";

    private Repository repository = new Repository();

    @Mock
    private BranchRepository branchRepository;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private BranchEventHandler branchEventHandler;

    @BeforeEach
    void beforeEach() {
    }

    @Nested
    class GivenNoExistingBranches {
        @Nested
        class WhenCreating {
            @Test
            void thenHandleBeforeCreateShouldFailOnEmptyBranch() {
                when(branchRepository.findByRepositoryAndName(null, null)).thenReturn(Optional.empty());
                Branch branch = new Branch();

                ApiException exception = assertThrows(ApiException.class, () -> branchEventHandler.handleBeforeCreate(branch));

                assertThat(exception, apiExceptionWithCode(
                        ErrorCode.PROPERTY_IS_NULL,
                        ErrorCode.PROPERTY_IS_NULL,
                        ErrorCode.PROPERTY_IS_NULL,
                        ErrorCode.PROPERTY_IS_NULL,
                        ErrorCode.PROPERTY_IS_NULL
                ));
            }

            @Test
            void thenHandleBeforeCreateShouldFailOnEmptyNameBranch() {
                when(branchRepository.findByRepositoryAndName(eq(null), eq(""))).thenReturn(Optional.empty());
                Branch branch = new Branch()
                        .setName("");

                ApiException exception = assertThrows(ApiException.class, () -> branchEventHandler.handleBeforeCreate(branch));

                assertThat(exception, apiExceptionWithCode(
                        ErrorCode.STRING_IS_EMPTY,
                        ErrorCode.PROPERTY_IS_NULL,
                        ErrorCode.PROPERTY_IS_NULL,
                        ErrorCode.PROPERTY_IS_NULL,
                        ErrorCode.PROPERTY_IS_NULL
                ));
            }

            @Test
            void thenHandleBeforeCreateShouldPassOnValidBranch() {
                when(branchRepository.findByRepositoryAndName(repository, BRANCH_NAME)).thenReturn(Optional.empty());
                Branch branch = new Branch()
                        .setRepository(repository)
                        .setName(BRANCH_NAME)
                        .setInitialBaseline(mock(Tag.class))
                        .setCurrentBaseline(mock(Tag.class))
                        .setCurrentTag(mock(Tag.class));

                branchEventHandler.handleBeforeCreate(branch);
            }
        }
    }

    @Nested
    class GivenExistingBranches {
        @Nested
        class WhenCreating {
            Branch branch = new Branch()
                    .setRepository(repository)
                    .setName(BRANCH_NAME)
                    .setInitialBaseline(mock(Tag.class))
                    .setCurrentBaseline(mock(Tag.class))
                    .setCurrentTag(mock(Tag.class));

            @Test
            void thenHandleBeforeCreateShouldFailOnNameAlreadyInUse() {
                when(branchRepository.findByRepositoryAndName(repository, BRANCH_NAME)).thenReturn(Optional.of(mock(Branch.class)));

                ApiException exception = assertThrows(ApiException.class, () -> branchEventHandler.handleBeforeCreate(branch));

                assertThat(exception, apiExceptionWithCode(ErrorCode.BRANCH_NAME_IN_USE));
            }
        }


        @Nested
        class WhenUpdating {
            String NEW_NAME = "new name";

            Tag tag = new Tag()
                    .setId(UUID.randomUUID());

            Branch preUpdate = new Branch()
                    .setId(UUID.randomUUID())
                    .setName(BRANCH_NAME)
                    .setRepository(repository)
                    .setInitialBaseline(tag)
                    .setCurrentBaseline(tag)
                    .setCurrentTag(tag);

            Branch branch = new Branch()
                    .setId(preUpdate.getId())
                    .setName(BRANCH_NAME)
                    .setRepository(repository)
                    .setInitialBaseline(tag)
                    .setCurrentBaseline(tag)
                    .setCurrentTag(tag);

            @Test
            void thenHandleBeforeSaveShouldFailOnNameAlreadyInUse() {
                branch.setName(NEW_NAME);

                when(branchRepository.findById(any())).thenReturn(Optional.of(preUpdate));
                when(branchRepository.findByRepositoryAndName(repository, NEW_NAME)).thenReturn(Optional.of(mock(Branch.class)));

                ApiException exception = assertThrows(ApiException.class, () -> branchEventHandler.handleBeforeSave(branch));

                assertThat(exception, apiExceptionWithCode(ErrorCode.BRANCH_NAME_IN_USE));
            }

            @Test
            void thenHandleBeforeSaveShouldFailOnChangingRepository() {
                branch.setRepository(mock(Repository.class));

                when(branchRepository.findById(any())).thenReturn(Optional.of(preUpdate));

                ApiException exception = assertThrows(ApiException.class, () -> branchEventHandler.handleBeforeSave(branch));

                assertThat(exception, apiExceptionWithCode(ErrorCode.REPOSITORY_FIXED));
            }

            @Test
            void thenHandleBeforeSaveShouldFailOnChangingInitialBaseline() {
                branch.setInitialBaseline(new Tag()
                        .setId(UUID.randomUUID())
                        .setName("someNewTag"));

                when(branchRepository.findById(any())).thenReturn(Optional.of(preUpdate));

                ApiException exception = assertThrows(ApiException.class, () -> branchEventHandler.handleBeforeSave(branch));

                assertThat(exception, apiExceptionWithCode(ErrorCode.BRANCH_BASELINE_FIXED));
            }

            @Test
            void thenHandleBeforeSaveShouldPassOnChangeName() {
                branch.setName(NEW_NAME);

                when(branchRepository.findById(any())).thenReturn(Optional.of(preUpdate));
                when(branchRepository.findByRepositoryAndName(repository, NEW_NAME)).thenReturn(Optional.empty());

                branchEventHandler.handleBeforeSave(branch);
            }
        }
    }
}

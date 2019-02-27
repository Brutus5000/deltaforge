package net.brutus5000.deltaforge.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import net.brutus5000.deltaforge.client.api.ApiClient;
import net.brutus5000.deltaforge.client.error.CheckoutException;
import net.brutus5000.deltaforge.client.io.DownloadService;
import net.brutus5000.deltaforge.client.model.Patch;
import net.brutus5000.deltaforge.client.model.Repository;
import net.brutus5000.deltaforge.client.model.Tag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Paths;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeltaforgeClientTest {
    @Mock
    ApiClient apiClient;

    @Mock
    DeltaforgeClientProperties properties;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    DownloadService downloadService;

    @Mock
    RepositoryService repositoryService;

    DeltaforgeClient underTest;

    @BeforeEach
    void beforeEach() throws Exception {
        underTest = new DeltaforgeClient(properties, objectMapper, downloadService, repositoryService);
    }

    @AfterEach
    void afterEach() throws Exception {
        verifyNoMoreInteractions(apiClient, properties, objectMapper, downloadService, repositoryService);
    }

    @Test
    void addRepositorySuccess() throws Exception {
        final String REPO_NAME = "myRepo";
        final String REPO_PATH = "someRepoPath";
        final String SOURCE_FILE_PATH = "someSourceFilesPath";

        final Repository localRepository = new Repository()
                .setName(REPO_NAME);

        doReturn(REPO_PATH).when(properties).getRootDirectory();
        doReturn(localRepository).when(repositoryService).initialize(eq(REPO_NAME), eq(Paths.get(REPO_PATH, REPO_NAME)), eq(Paths.get(SOURCE_FILE_PATH)));

        Repository result = underTest.addRepository(REPO_NAME, Paths.get(SOURCE_FILE_PATH));

        assertThat(result, is(localRepository));

        verify(repositoryService).initialize(eq(REPO_NAME), eq(Paths.get(REPO_PATH, REPO_NAME)), eq(Paths.get(SOURCE_FILE_PATH)));
    }

    @Test
    void checkoutTagIOException() throws Exception {
        final String TAG_NAME = "myTag";
        final String EXCEPTION_MESSAGE = "someMessage";

        doThrow(new CheckoutException(EXCEPTION_MESSAGE, null)).when(repositoryService).getPatchPath(any(), anyString());

        Repository localRepository = new Repository();

        CheckoutException checkoutException = assertThrows(CheckoutException.class, () -> underTest.checkoutTag(localRepository, TAG_NAME));

        assertThat(checkoutException.getMessage(), is(EXCEPTION_MESSAGE));
    }

    @Test
    void checkoutTagAlreadyCheckedOut() throws Exception {
        final String TAG_NAME = "myTag";

        Repository repository = new Repository()
                .setCurrentTag(TAG_NAME);

        underTest.checkoutTag(repository, TAG_NAME);
    }

    @Test
    void checkoutTagSuccess() throws Exception {
        final String TAG_NAME = "myTag";

        Repository localRepository = new Repository();

        doReturn(Lists.newArrayList(mock(Patch.class), mock(Patch.class))).when(repositoryService).getPatchPath(any(), anyString());

        underTest.checkoutTag(localRepository, TAG_NAME);

        verify(repositoryService).getPatchPath(localRepository, TAG_NAME);
        verify(repositoryService, times(2)).downloadPatchIfMissing(eq(localRepository), any());
        verify(repositoryService, times(2)).applyPatch(eq(localRepository), any());
    }

    @Test
    void checkoutLatestIOException() throws Exception {
        final String BRANCH_NAME = "myBranch";
        final String EXCEPTION_MESSAGE = "someMessage";

        doThrow(new CheckoutException(EXCEPTION_MESSAGE, null)).when(repositoryService).refreshTagGraph(any());

        Repository repository = new Repository();

        CheckoutException checkoutException = assertThrows(CheckoutException.class, () -> underTest.checkoutLatest(repository, BRANCH_NAME));

        assertThat(checkoutException.getMessage(), is(EXCEPTION_MESSAGE));
    }

    @Test
    void checkoutLatestSuccess() throws Exception {
        final String BRANCH_NAME = "myBranch";
        final String TAG_NAME = "myTag";

        Repository repository = mock(Repository.class);
        doReturn(Optional.of(new Tag().setName(TAG_NAME))).when(repository).getLatestTag(BRANCH_NAME);

        doReturn(Lists.newArrayList(mock(Patch.class), mock(Patch.class))).when(repositoryService).getPatchPath(any(), anyString());

        underTest.checkoutLatest(repository, BRANCH_NAME);

        verify(repositoryService).refreshTagGraph(repository);
        verify(repositoryService).getPatchPath(repository, TAG_NAME);
        verify(repositoryService, times(2)).downloadPatchIfMissing(eq(repository), any());
        verify(repositoryService, times(2)).applyPatch(eq(repository), any());
    }
}

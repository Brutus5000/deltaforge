package net.brutus5000.deltaforge.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.brutus5000.deltaforge.client.api.ApiClient;
import net.brutus5000.deltaforge.client.error.CheckoutException;
import net.brutus5000.deltaforge.client.error.InitializeException;
import net.brutus5000.deltaforge.client.io.DownloadService;
import net.brutus5000.deltaforge.client.model.Patch;
import net.brutus5000.deltaforge.client.model.Repository;
import net.brutus5000.deltaforge.client.model.Tag;
import net.brutus5000.deltaforge.client.model.TagType;
import net.brutus5000.deltaforge.client.patching.PatchGraph;
import net.brutus5000.deltaforge.client.patching.PatchGraphFactory;
import net.brutus5000.deltaforge.client.patching.PatchService;
import net.brutus5000.deltaforge.patching.io.IoService;
import net.brutus5000.deltaforge.patching.io.ValidationService;
import net.brutus5000.deltaforge.patching.meta.patch.PatchMetadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.getField;

@ExtendWith(MockitoExtension.class)
class RepositoryServiceTest {

    private static final String REPOSITORY_NAME = "someRepository";
    private static final String TAG_NAME = "someTag";

    @Mock
    private DeltaforgeClientProperties propertiesMock;

    @Mock
    private IoService ioServiceMock;

    @Mock
    private ObjectMapper objectMapperMock;

    @Mock
    private ValidationService validationServiceMock;

    @Mock
    private ApiClient apiClientMock;

    @Mock
    private DownloadService downloadServiceMock;

    @Mock
    private PatchService patchServiceMock;

    @Mock
    private PatchGraphFactory patchGraphFactoryMock;

    private RepositoryService underTest;

    @BeforeEach
    void beforeEach() {
        underTest = new RepositoryService(propertiesMock, ioServiceMock, objectMapperMock, validationServiceMock,
                apiClientMock, downloadServiceMock, patchServiceMock, patchGraphFactoryMock);
    }

    @AfterEach
    void afterEach() {
        verifyNoMoreInteractions(propertiesMock, ioServiceMock, objectMapperMock, validationServiceMock,
                apiClientMock, downloadServiceMock, patchServiceMock, patchGraphFactoryMock);
    }

    @Nested
    class GivenNoCachedRepository {

        @Nested
        class WhenFindingByName {
            @Test
            void withNoExistingFileShouldReturnEmpty() throws Exception {
                doReturn("rootDirectory").when(propertiesMock).getRootDirectory();
                doReturn(false).when(ioServiceMock).isFile(any());

                Optional<Repository> result = underTest.findByName(REPOSITORY_NAME);

                verify(propertiesMock).getRootDirectory();
                verify(ioServiceMock).isFile(any());

                assertThat(result.isPresent(), is(false));
            }

            @Test
            void withExistingFileShouldLoadFromDisk() throws Exception {
                Repository repository = new Repository()
                        .setName(REPOSITORY_NAME);

                doReturn("rootDirectory").when(propertiesMock).getRootDirectory();
                doReturn(true).when(ioServiceMock).isFile(any());
                doReturn(repository).when(objectMapperMock).readValue(any(File.class), eq(Repository.class));

                Optional<Repository> result = underTest.findByName(REPOSITORY_NAME);

                verify(propertiesMock, times(2)).getRootDirectory();
                verify(propertiesMock).getServerContentUrl();
                verify(ioServiceMock).isFile(any());
                verify(objectMapperMock).readValue(any(File.class), eq(Repository.class));
                verify(patchGraphFactoryMock).buildPatchGraph(repository);

                assertThat(result.isPresent(), is(true));
                assertThat(result.get(), is(repository));
            }

            @Test
            void whenLoadedTwiceShouldLoadFromCache() throws Exception {
                withExistingFileShouldLoadFromDisk();

                Optional<Repository> result = underTest.findByName(REPOSITORY_NAME);

                assertThat(result.isPresent(), is(true));

                // No verifications here, as it should be loaded from cache the second time
                // BeforeEach checks for no more interactions
            }
        }

        @Nested
        class WhenInitializing {
            @Test
            void withExistingDirectoryInitializeShouldFail() {
                doReturn(true).when(ioServiceMock).isDirectory(any());

                assertThrows(InitializeException.class, () -> underTest.initialize(REPOSITORY_NAME,
                        mock(Path.class), mock(Path.class)));
            }

            @Test
            void withApiCallNoResultInitializeShouldFail() throws Exception {
                doReturn(false).when(ioServiceMock).isDirectory(any());
                doReturn(Optional.empty()).when(apiClientMock).getRepository(REPOSITORY_NAME);

                assertThrows(InitializeException.class, () -> underTest.initialize(REPOSITORY_NAME,
                        mock(Path.class), mock(Path.class)));
            }

            @Test
            void withApiCallFailingInitializeShouldFail() throws Exception {
                doReturn(false).when(ioServiceMock).isDirectory(any());
                doThrow(new IOException()).when(apiClientMock).getRepository(REPOSITORY_NAME);

                assertThrows(InitializeException.class, () -> underTest.initialize(REPOSITORY_NAME,
                        mock(Path.class), mock(Path.class)));

                verify(ioServiceMock).deleteQuietly(any());
            }

            @Test
            void withoutSourceTagInitializeShouldFail() throws Exception {
                Repository repository = new Repository()
                        .setName(REPOSITORY_NAME);

                doReturn(false).when(ioServiceMock).isDirectory(any());
                doReturn(Optional.of(repository)).when(apiClientMock).getRepository(REPOSITORY_NAME);

                assertThrows(InitializeException.class, () -> underTest.initialize(REPOSITORY_NAME,
                        mock(Path.class), mock(Path.class)));

                verify(apiClientMock).getRepository(REPOSITORY_NAME);
            }

            @Test
            void withPreconditionsMetShouldReturnRepository() throws Exception {
                Tag sourceTag = new Tag()
                        .setName(TAG_NAME)
                        .setType(TagType.SOURCE);

                Repository repository = new Repository()
                        .setName(REPOSITORY_NAME);

                repository.getTags().add(sourceTag);

                doReturn(false).when(ioServiceMock).isDirectory(any());
                doReturn(Optional.of(repository)).when(apiClientMock).getRepository(REPOSITORY_NAME);
                doReturn(true).when(validationServiceMock).validateChecksums(any(), any());

                Repository result = underTest.initialize(REPOSITORY_NAME, Paths.get("."), mock(Path.class));

                assertThat(result.getName(), is(REPOSITORY_NAME));

                verify(propertiesMock).getRootDirectory();
                verify(propertiesMock).getServerContentUrl();
                verify(apiClientMock).getRepository(REPOSITORY_NAME);
                verify(ioServiceMock).copyDirectory(any(), any());
                verify(objectMapperMock).writeValue(any(File.class), eq(repository));
                verify(patchGraphFactoryMock).buildPatchGraph(repository);
            }
        }
    }

    @Nested
    class GivenLoadedRepository {
        Repository cachedRepository;

        RepositoryService.RepositoryCacheItem repositoryCacheItem;

        @BeforeEach
        void beforeEach() {
            cachedRepository = new Repository()
                    .setName(REPOSITORY_NAME)
                    .setUrl("http://localhost")
                    .setMainDirectory(Paths.get("."));

            repositoryCacheItem = new RepositoryService.RepositoryCacheItem();

            Map<Repository, RepositoryService.RepositoryCacheItem> repositoryCache =
                    (Map<Repository, RepositoryService.RepositoryCacheItem>) getField(underTest, "repositoryCache");
            repositoryCache.put(cachedRepository, repositoryCacheItem);
        }

        @Nested
        class WhenInitializing {
            @Test
            void withRepositoryInCacheInitializeShouldThrowException() {
                assertThrows(InitializeException.class, () ->
                        underTest.initialize(REPOSITORY_NAME, mock(Path.class), mock(Path.class)));
            }
        }

        @Nested
        class WhenRefreshingGraph {
            @Test
            void withApiClientNoResultShouldThrowIOException() throws Exception {
                doReturn(Optional.empty()).when(apiClientMock).getRepository(any());

                assertThrows(IOException.class, () -> underTest.refreshTagGraph(mock(Repository.class)));
            }

            @Test
            void withApiClientFailingShouldRethrow() throws Exception {
                IOException exception = new IOException();

                doThrow(exception).when(apiClientMock).getRepository(any());

                IOException result = assertThrows(IOException.class, () -> underTest.refreshTagGraph(mock(Repository.class)));

                assertThat(result, sameInstance(exception));
            }

            @Test
            void shouldPass() throws Exception {
                doReturn(Optional.of(mock(Repository.class))).when(apiClientMock).getRepository(any());
                PatchGraph patchGraphMock = mock(PatchGraph.class);
                repositoryCacheItem.setPatchGraph(patchGraphMock);

                underTest.refreshTagGraph(cachedRepository);

                verify(apiClientMock).getRepository(any());
                verify(patchGraphMock).refreshGraph();
            }
        }

        @Nested
        class WhenCalculatingPatchPath {
            private static final String CURRENT_TAG = "currentTag";

            @Test
            void withUnknownCurrentTagShouldFail() {
                cachedRepository.setCurrentTag("currentTag");

                CheckoutException result = assertThrows(CheckoutException.class, () -> underTest.calculatePatchPath(cachedRepository, TAG_NAME));

                assertThat(result.getMessage(), is("Unknown tag: currentTag"));
            }

            @Test
            void withUnknownTargetTagShouldFail() {
                cachedRepository.setCurrentTag(CURRENT_TAG);
                cachedRepository.getTags().add(new Tag().setName(CURRENT_TAG));

                CheckoutException result = assertThrows(CheckoutException.class, () -> underTest.calculatePatchPath(cachedRepository, TAG_NAME));

                assertThat(result.getMessage(), is("Unknown tag: " + TAG_NAME));
            }

            @Test
            void withKnownTagsShouldBuildList() {
                cachedRepository.setCurrentTag(CURRENT_TAG);
                Tag currentTag = new Tag().setName(CURRENT_TAG);
                cachedRepository.getTags().add(currentTag);
                Tag targetTag = new Tag().setName(TAG_NAME);
                cachedRepository.getTags().add(targetTag);

                PatchGraph patchGraphMock = mock(PatchGraph.class);
                repositoryCacheItem.setPatchGraph(patchGraphMock);
                List<Patch> patchPathMock = mock(List.class);
                doReturn(patchPathMock).when(patchGraphMock).getPatchPath(currentTag, targetTag);

                List<Patch> result = underTest.calculatePatchPath(cachedRepository, TAG_NAME);

                assertThat(result, sameInstance(patchPathMock));
            }
        }

        @Nested
        class WhenDownloadingPatch {
            @Test
            void withFileOneDiskShouldDoNothing() throws Exception {
                doReturn(true).when(ioServiceMock).isFile(any());

                Patch patch = new Patch()
                        .setTo(new Tag().setName("tagTo"))
                        .setFrom(new Tag().setName("tagFrom"));
                underTest.downloadPatchIfMissing(cachedRepository, patch);

                verify(ioServiceMock).createDirectories(any());
            }

            @Test
            void withoutFileShouldDownload() throws Exception {
                doReturn(false).when(ioServiceMock).isFile(any());

                Patch patch = new Patch()
                        .setTo(new Tag().setName("tagTo"))
                        .setFrom(new Tag().setName("tagFrom"));

                underTest.downloadPatchIfMissing(cachedRepository, patch);

                verify(ioServiceMock).createDirectories(any());
                verify(downloadServiceMock, times(2)).download(any(), any());
            }
        }

        @Nested
        class WhenApplyingPatch {
            @Test
            void shouldPass() throws Exception {
                Patch patch = new Patch()
                        .setRepository(cachedRepository)
                        .setMetadata(mock(PatchMetadata.class))
                        .setTo(new Tag().setName("tagTo"))
                        .setFrom(new Tag().setName("tagFrom"));

                underTest.applyPatch(cachedRepository, patch);

                verify(ioServiceMock, times(2)).createTempDirectory(any());
                verify(ioServiceMock).deleteDirectory(any());
                verify(ioServiceMock).moveDirectory(any(), any());
                verify(ioServiceMock).unzip(any(), any());
                verify(patchServiceMock).applyPatch(any());
            }
        }
    }
}

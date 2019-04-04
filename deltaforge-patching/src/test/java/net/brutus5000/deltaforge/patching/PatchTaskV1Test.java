package net.brutus5000.deltaforge.patching;

import com.google.common.collect.Sets;
import net.brutus5000.deltaforge.patching.io.Bsdiff4Service;
import net.brutus5000.deltaforge.patching.io.IoService;
import net.brutus5000.deltaforge.patching.meta.patch.*;
import org.apache.commons.io.FileUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatchTaskV1Test {
    private static final Path SELF_PATH = Paths.get(".");
    private static final String FILE_PREFIX = "./src/test/resources/";
    private static final String PATCH_NAME = "source_to_target";

    @Mock
    private Bsdiff4Service bsdiff4Service;
    private final Path rootSourceFolder;
    private final Path rootInitialBaselineFolder;
    private final Path rootTargetFolder;
    private final Path rootPatchFolder;
    @Mock
    private IoService ioService;
    private PatchTaskV1 instance;

    {
        rootPatchFolder = Paths.get(FILE_PREFIX + "testRepo/temp/patching/" + PATCH_NAME);
        rootSourceFolder = Paths.get(FILE_PREFIX + "testRepo/tags/source");
        rootInitialBaselineFolder = Paths.get(FILE_PREFIX + "testRepo/tags/initialBaseline");
        rootTargetFolder = Paths.get(FILE_PREFIX + "testRepo/tags/target");
    }

    @BeforeEach
    void beforeEach() throws Exception {

        instance = new PatchTaskV1(
                bsdiff4Service, ioService, rootSourceFolder,
                rootInitialBaselineFolder,
                rootTargetFolder,
                rootPatchFolder,
                "testRepository"
        );

        if (Files.isDirectory(rootPatchFolder)) {
            FileUtils.deleteQuietly(rootPatchFolder.toFile());
        }
    }

    @Nested
    class ApplyFile {
        private static final String FILE_NAME = "someFile";
        private final Path sourceFile;
        private final Path targetFile;
        private final Path patchFile;
        private final Path initialBaselineFile;
        private PatchFileItem patchItem;

        {
            sourceFile = rootSourceFolder.resolve(SELF_PATH).resolve(FILE_NAME);
            targetFile = rootTargetFolder.resolve(SELF_PATH).resolve(FILE_NAME);
            patchFile = rootPatchFolder.resolve(SELF_PATH).resolve(FILE_NAME);
            initialBaselineFile = rootInitialBaselineFolder.resolve(SELF_PATH).resolve(FILE_NAME);
        }

        @BeforeEach
        void beforeEach() {
            patchItem = new PatchFileItem()
                    .setName(FILE_NAME);
        }

        @Test
        void testUnchanged() throws Exception {
            patchItem.setAction(PatchAction.UNCHANGED);

            instance.applyFile(patchItem, SELF_PATH);
            verify(ioService).copy(sourceFile, targetFile);
            verify(ioService).crc32(sourceFile);
            verifyNoMoreInteractions(ioService);
            verifyZeroInteractions(bsdiff4Service);
        }

        @Test
        void testAdded() throws Exception {
            patchItem.setAction(PatchAction.ADD);

            instance.applyFile(patchItem, SELF_PATH);
            verify(ioService).copy(patchFile, targetFile);
            verify(ioService).crc32(targetFile);
            verifyNoMoreInteractions(ioService);
            verifyZeroInteractions(bsdiff4Service);
        }

        @Test
        void testRemoved() throws Exception {
            patchItem.setAction(PatchAction.REMOVE);

            instance.applyFile(patchItem, SELF_PATH);
            verifyNoMoreInteractions(ioService);
            verifyZeroInteractions(bsdiff4Service);
        }

        @Test
        void testBsdiff() throws Exception {
            patchItem.setAction(PatchAction.BSDIFF);

            instance.applyFile(patchItem, SELF_PATH);

            verify(ioService).crc32(sourceFile);
            verify(ioService).crc32(targetFile);
            verifyNoMoreInteractions(ioService);

            verify(bsdiff4Service).applyPatch(sourceFile, targetFile, patchFile);
            verifyNoMoreInteractions(bsdiff4Service);
        }

        @Test
        void testBsdiffBaseline() throws Exception {
            patchItem.setAction(PatchAction.BSDIFF_FROM_INITIAL_BASELINE);

            instance.applyFile(patchItem, SELF_PATH);

            verify(ioService).crc32(initialBaselineFile);
            verify(ioService).crc32(targetFile);
            verifyNoMoreInteractions(ioService);

            verify(bsdiff4Service).applyPatch(initialBaselineFile, targetFile, patchFile);
            verifyNoMoreInteractions(bsdiff4Service);
        }

        @Test
        void testCrcMismatch() throws Exception {
            patchItem.setAction(PatchAction.ADD);
            patchItem.setBaseCrc("correctValue");
            when(ioService.crc32(any())).thenReturn("wrongValue");

            assertThrows(CrcMismatchException.class, () -> instance.applyFile(patchItem, SELF_PATH));
        }

        @Test
        void testInvalidActionStates() throws Exception {
            assertAll(
                    () -> assertThrows(IllegalStateException.class, () -> {
                        patchItem.setAction(PatchAction.DELTA);
                        instance.applyFile(patchItem, SELF_PATH);
                    }),
                    () -> assertThrows(IllegalStateException.class, () -> {
                        patchItem.setAction(PatchAction.COMPRESSED_FILE);
                        instance.applyFile(patchItem, SELF_PATH);
                    })
            );
        }
    }

    @Nested
    class ApplyDirectory {

        private static final String DIRECTORY_NAME = "someDirectory";
        private final Path targetFolder;
        private final Path patchFolder;
        private PatchDirectoryItem patchItem;

        {
            targetFolder = rootTargetFolder.resolve(SELF_PATH).resolve(DIRECTORY_NAME);
            patchFolder = rootPatchFolder.resolve(SELF_PATH).resolve(DIRECTORY_NAME);
        }

        @BeforeEach
        void beforeEach() {
            patchItem = new PatchDirectoryItem()
                    .setName(DIRECTORY_NAME);
        }

        @Test
        void testAdd() throws Exception {
            patchItem.setAction(PatchAction.ADD);

            instance.applyDirectory(patchItem, SELF_PATH);

            verify(ioService).copyDirectory(patchFolder, targetFolder);
            verifyNoMoreInteractions(ioService);
            verifyZeroInteractions(bsdiff4Service);
        }

        @Test
        void testRemove() throws Exception {
            patchItem.setAction(PatchAction.REMOVE);

            instance.applyDirectory(patchItem, SELF_PATH);

            verifyZeroInteractions(ioService);
            verifyZeroInteractions(bsdiff4Service);
        }

        @Test
        void testDelta() throws Exception {
            patchItem.setAction(PatchAction.DELTA);
            String folderName = "added";
            patchItem.setItems(Sets.newHashSet(new PatchDirectoryItem()
                    .setAction(PatchAction.ADD)
                    .setName(folderName)));

            instance.applyDirectory(patchItem, SELF_PATH);

            verify(ioService).copyDirectory(patchFolder.resolve(folderName), targetFolder.resolve(folderName));
            verifyNoMoreInteractions(ioService);
            verifyZeroInteractions(bsdiff4Service);
        }

        @Test
        void testInvalidActionStates() throws Exception {
            assertAll(
                    () -> assertThrows(IllegalStateException.class, () -> {
                        patchItem.setAction(PatchAction.UNCHANGED);
                        instance.applyDirectory(patchItem, SELF_PATH);
                    }),
                    () -> assertThrows(IllegalStateException.class, () -> {
                        patchItem.setAction(PatchAction.BSDIFF);
                        instance.applyDirectory(patchItem, SELF_PATH);
                    }),
                    () -> assertThrows(IllegalStateException.class, () -> {
                        patchItem.setAction(PatchAction.BSDIFF_FROM_INITIAL_BASELINE);
                        instance.applyDirectory(patchItem, SELF_PATH);
                    }),
                    () -> assertThrows(IllegalStateException.class, () -> {
                        patchItem.setAction(PatchAction.COMPRESSED_FILE);
                        instance.applyDirectory(patchItem, SELF_PATH);
                    })
            );
        }
    }

    @Nested
    class ApplyZipFile {
        private static final String FILE_NAME = "someFile.zip";
        private final Path sourceZipFile;
        private final Path targetZipFile;
        private final Path patchZipFile;
        private final Path initialBaselineZipFile;
        private PatchCompressedItem patchItem;

        {
            sourceZipFile = rootSourceFolder.resolve(SELF_PATH).resolve(FILE_NAME);
            targetZipFile = rootTargetFolder.resolve(SELF_PATH).resolve(FILE_NAME);
            patchZipFile = rootPatchFolder.resolve(SELF_PATH).resolve(FILE_NAME);
            initialBaselineZipFile = rootInitialBaselineFolder.resolve(SELF_PATH).resolve(FILE_NAME);
        }

        @BeforeEach
        void beforeEach() {
            patchItem = new PatchCompressedItem()
                    .setName(FILE_NAME);
        }

        @Test
        void testApplyWithoutItems() throws Exception {
            patchItem.setItems(Sets.newHashSet());
            patchItem.setAction(PatchAction.DELTA);

            final Path tempRootDirectory = Paths.get("someTemporaryZipPath");
            final Path sourceFolder = tempRootDirectory.resolve("source");
            final Path targetFolder = tempRootDirectory.resolve("target");
            final Path initialBaselineFolder = tempRootDirectory.resolve("initialBaseline");
            final Path patchFolder = tempRootDirectory.resolve("patch");

            doReturn(sourceFolder).when(ioService).createDirectories(sourceFolder);
            doReturn(targetFolder).when(ioService).createDirectories(targetFolder);
            doReturn(initialBaselineFolder).when(ioService).createDirectories(initialBaselineFolder);
            doReturn(patchFolder).when(ioService).createDirectories(patchFolder);
            doReturn(tempRootDirectory).when(ioService).createTempDirectory("deltaforge_zip_root_");

            instance.applyZipFile(patchItem, SELF_PATH);

            verify(ioService).unzip(sourceZipFile, sourceFolder);
            verify(ioService).unzip(patchZipFile, patchFolder);
            verify(ioService).zip(eq(targetFolder), any(Path.class));
            verify(ioService).deleteDirectory(tempRootDirectory);
            verifyNoMoreInteractions(ioService);
            verifyZeroInteractions(bsdiff4Service);
        }
    }

    private Matcher<PatchItem> patchItemWith(Class<? extends PatchItem> clazz, String name, PatchAction action) {
        return new TypeSafeDiagnosingMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description
                        .appendText("type should be ").appendValue(clazz)
                        .appendText(" and getName should be ").appendValue(name)
                        .appendText(" and getPatchAction should be ").appendValue(action);
            }

            @Override
            protected boolean matchesSafely(final PatchItem item, final Description mismatchDescription) {
                mismatchDescription
                        .appendText(" type was ").appendValue(item.getClass())
                        .appendText(" and getName was ").appendValue(item.getName())
                        .appendText(" and getPatchAction was").appendValue(item.getAction());

                return item.getClass() == clazz
                        && Objects.equals(name, item.getName())
                        && Objects.equals(action, item.getAction());
            }
        };
    }
}

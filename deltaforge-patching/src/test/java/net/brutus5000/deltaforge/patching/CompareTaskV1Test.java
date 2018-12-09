package net.brutus5000.deltaforge.patching;

import net.brutus5000.deltaforge.patching.meta.*;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompareTaskV1Test {
    private final static String FILENAME_UNCHANGED = "unchanged.txt";
    private final static String FILENAME_NEW = "new.txt";
    private final static String FILENAME_REMOVED_FROM_SOURCE = "removed-from-source.txt";
    private final static String FILENAME_MODIFIED_FROM_SOURCE = "modified-from-source.txt";
    private final static String DIRECTORY_UNCHANGED = "unchanged-folder";
    private final static String DIRECTORY_NEW = "new-folder";
    private final static String DIRECTORY_REMOVED = "removed-folder";
    private final static String DIRECTORY_ZIP = "zip";
    private final static String FILENAME_MODIFIED_FROM_INITIAL = "modified-from-initial.txt";
    private final static String ARCHIVE_UNCHANGED = "unchanged.zip";
    private final static String ARCHIVE_NEW = "new.zip";
    private final static String ARCHIVE_REMOVED_FROM_SOURCE = "removed-from-source.zip";
    private final static String ARCHIVE_MODIFIED_FROM_SOURCE = "modified-from-source.zip";
    private final static String ARCHIVE_MODIFIED_FROM_INITIAL = "modified-from-initial.zip";
    private final static String FOLDER_ZIP = "zip";

    @Mock
    private Bsdiff4Service bsdiff4Service;

    CompareTaskV1 instance;


    @BeforeEach
    void beforeEach() throws Exception {
        String FILE_PREFIX = "./src/test/resources/";
        String PATCH_NAME = "source_to_target";
        Path patchFolder = Paths.get(FILE_PREFIX + "testRepo/temp/patching/" + PATCH_NAME);
        instance = new CompareTaskV1(
                bsdiff4Service, Paths.get(FILE_PREFIX + "testRepo/tags/source"),
                Paths.get(FILE_PREFIX + "testRepo/tags/initialBaseline"),
                Paths.get(FILE_PREFIX + "testRepo/tags/target"),
                patchFolder,
                "testRepository",
                "fromTag",
                "toTag"
        );

        if (Files.isDirectory(patchFolder)) {
            Files.walk(patchFolder)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @Nested
    class FileComparison {
        @Test
        void compareFileUnchanged() throws Exception {
            PatchFileItem fileItem = instance.compareFile(Paths.get(FILENAME_UNCHANGED));

            assertAll("fileItem",
                    () -> assertEquals(FILENAME_UNCHANGED, fileItem.getName()),
                    () -> assertEquals(PatchAction.UNCHANGED, fileItem.getAction()),
                    () -> assertEquals("0xb6651dae", fileItem.getBaseCrc()),
                    () -> assertEquals("0xb6651dae", fileItem.getTargetCrc())
            );
        }

        @Test
        void compareFileNew() throws Exception {
            PatchFileItem fileItem = instance.compareFile(Paths.get(FILENAME_NEW));

            assertAll("fileItem",
                    () -> assertEquals(FILENAME_NEW, fileItem.getName()),
                    () -> assertEquals(PatchAction.ADD, fileItem.getAction()),
                    () -> assertNull(fileItem.getBaseCrc()),
                    () -> assertEquals("0xfe444d37", fileItem.getTargetCrc())
            );
        }

        @Test
        void compareFileRemovedFromSource() throws Exception {
            PatchFileItem fileItem = instance.compareFile(Paths.get(FILENAME_REMOVED_FROM_SOURCE));

            assertAll("fileItem",
                    () -> assertEquals(FILENAME_REMOVED_FROM_SOURCE, fileItem.getName()),
                    () -> assertEquals(PatchAction.REMOVE, fileItem.getAction()),
                    () -> assertNull(fileItem.getBaseCrc()),
                    () -> assertNull(fileItem.getTargetCrc())
            );
        }

        @Test
        void compareFileModifiedFromSource() throws Exception {
            PatchFileItem fileItem = instance.compareFile(Paths.get(FILENAME_MODIFIED_FROM_SOURCE));

            assertAll("fileItem",
                    () -> assertEquals(FILENAME_MODIFIED_FROM_SOURCE, fileItem.getName()),
                    () -> assertEquals(PatchAction.BSDIFF, fileItem.getAction()),
                    () -> assertEquals("0x5c428a3d", fileItem.getBaseCrc()),
                    () -> assertEquals("0x2b7fbb9d", fileItem.getTargetCrc())
            );

            verify(bsdiff4Service).createPatch(
                    argThat(path -> path.toString().endsWith(FILENAME_MODIFIED_FROM_SOURCE)),
                    argThat(path -> path.toString().endsWith(FILENAME_MODIFIED_FROM_SOURCE)),
                    argThat(path -> path.toString().endsWith(FILENAME_MODIFIED_FROM_SOURCE))
            );
        }

        @Test
        void compareFileModifiedFromInitial() throws Exception {
            PatchFileItem fileItem = instance.compareFile(Paths.get(FILENAME_MODIFIED_FROM_INITIAL));

            assertAll("fileItem",
                    () -> assertEquals(FILENAME_MODIFIED_FROM_INITIAL, fileItem.getName()),
                    () -> assertEquals(PatchAction.BSDIFF_FROM_INITIAL_BASELINE, fileItem.getAction()),
                    () -> assertEquals("0xffc5f2e9", fileItem.getBaseCrc()),
                    () -> assertEquals("0x6fe9f2d6", fileItem.getTargetCrc())
            );

            verify(bsdiff4Service).createPatch(
                    argThat(path -> path.toString().endsWith(FILENAME_MODIFIED_FROM_INITIAL)),
                    argThat(path -> path.toString().endsWith(FILENAME_MODIFIED_FROM_INITIAL)),
                    argThat(path -> path.toString().endsWith(FILENAME_MODIFIED_FROM_INITIAL))
            );
        }

        @Test
        void isCompressed() throws Exception {
            String FILE_PREFIX = "./src/test/resources/testArchiveFiles";

            assertAll("content types",
                    () -> assertTrue(instance.isZipFile(Paths.get(FILE_PREFIX, "archive.zip"))),
                    () -> assertFalse(instance.isZipFile(Paths.get(FILE_PREFIX, "archive.7z")))
            );
        }
    }

    @Nested
    class DirectoryComparison {
        @Test
        void compareDirectories() throws Exception {
            PatchDirectoryItem directoryItem = instance.compareDirectory(Paths.get("."));

            assertThat(directoryItem.getItems(), containsInAnyOrder(
                    patchItemWith(PatchFileItem.class, FILENAME_UNCHANGED, PatchAction.UNCHANGED),
                    patchItemWith(PatchFileItem.class, FILENAME_NEW, PatchAction.ADD),
                    patchItemWith(PatchFileItem.class, FILENAME_REMOVED_FROM_SOURCE, PatchAction.REMOVE),
                    patchItemWith(PatchFileItem.class, FILENAME_MODIFIED_FROM_SOURCE, PatchAction.BSDIFF),
                    patchItemWith(PatchFileItem.class, FILENAME_MODIFIED_FROM_INITIAL, PatchAction.BSDIFF_FROM_INITIAL_BASELINE),
                    patchItemWith(PatchDirectoryItem.class, DIRECTORY_UNCHANGED, PatchAction.DELTA),
                    patchItemWith(PatchDirectoryItem.class, DIRECTORY_NEW, PatchAction.ADD),
                    patchItemWith(PatchDirectoryItem.class, DIRECTORY_REMOVED, PatchAction.REMOVE),
                    patchItemWith(PatchDirectoryItem.class, DIRECTORY_ZIP, PatchAction.DELTA)
            ));
        }
    }

    @Nested
    class ArchiveComparison {
        @Test
        void compareDirectories() throws Exception {
            PatchDirectoryItem directoryItem = instance.compareDirectory(Paths.get(FOLDER_ZIP));

            assertThat(directoryItem.getItems(), containsInAnyOrder(
                    patchItemWith(PatchCompressedItem.class, ARCHIVE_UNCHANGED, PatchAction.UNCHANGED),
                    patchItemWith(PatchCompressedItem.class, ARCHIVE_NEW, PatchAction.ADD),
                    patchItemWith(PatchCompressedItem.class, ARCHIVE_REMOVED_FROM_SOURCE, PatchAction.REMOVE),
                    patchItemWith(PatchCompressedItem.class, ARCHIVE_MODIFIED_FROM_SOURCE, PatchAction.COMPRESSED_FILE),
                    patchItemWith(PatchCompressedItem.class, ARCHIVE_MODIFIED_FROM_INITIAL, PatchAction.COMPRESSED_FILE)
            ));

            verify(bsdiff4Service, times(2)).createPatch(any(), any(), any());
        }

        @Test
        void compareZipUnchanged() throws Exception {
            PatchCompressedItem item = instance.compareZipFile(Paths.get(FOLDER_ZIP, ARCHIVE_UNCHANGED));

            assertAll("item",
                    () -> assertEquals(ARCHIVE_UNCHANGED, item.getName()),
                    () -> assertEquals(PatchAction.UNCHANGED, item.getAction())
            );

            verify(bsdiff4Service, never()).createPatch(any(Path.class), any(Path.class), any(Path.class));
        }

        @Test
        void compareZipNew() throws Exception {
            PatchCompressedItem item = instance.compareZipFile(Paths.get(FOLDER_ZIP, ARCHIVE_NEW));

            assertAll("item",
                    () -> assertEquals(ARCHIVE_NEW, item.getName()),
                    () -> assertEquals(PatchAction.ADD, item.getAction())
            );

            verify(bsdiff4Service, never()).createPatch(any(Path.class), any(Path.class), any(Path.class));
        }

        @Test
        void compareZipRemovedFromSource() throws Exception {
            PatchCompressedItem item = instance.compareZipFile(Paths.get(FOLDER_ZIP, ARCHIVE_REMOVED_FROM_SOURCE));

            assertAll("item",
                    () -> assertEquals(ARCHIVE_REMOVED_FROM_SOURCE, item.getName()),
                    () -> assertEquals(PatchAction.REMOVE, item.getAction())
            );

            verify(bsdiff4Service, never()).createPatch(any(Path.class), any(Path.class), any(Path.class));
        }

        @Test
        void compareZipModifiedFromSource() throws Exception {
            PatchCompressedItem item = instance.compareZipFile(Paths.get(FOLDER_ZIP, ARCHIVE_MODIFIED_FROM_SOURCE));

            assertAll("item",
                    () -> assertEquals(ARCHIVE_MODIFIED_FROM_SOURCE, item.getName()),
                    () -> assertEquals(PatchAction.COMPRESSED_FILE, item.getAction())
            );

            verify(bsdiff4Service).createPatch(any(Path.class), any(Path.class), any(Path.class));
        }

        @Test
        void compareZipModifiedFromInitial() throws Exception {
            PatchCompressedItem item = instance.compareZipFile(Paths.get(FOLDER_ZIP, ARCHIVE_MODIFIED_FROM_INITIAL));

            assertAll("item",
                    () -> assertEquals(ARCHIVE_MODIFIED_FROM_INITIAL, item.getName()),
                    () -> assertEquals(PatchAction.COMPRESSED_FILE, item.getAction())
            );

            verify(bsdiff4Service).createPatch(any(Path.class), any(Path.class), any(Path.class));
        }
    }

    @Test
    void testCompare() throws Exception {
        PatchMetadata metadata = instance.compare();

        assertAll("metadata",
                () -> assertEquals(1, metadata.getProtocol()),
                () -> assertThat(metadata.getItems(), containsInAnyOrder(
                        patchItemWith(PatchFileItem.class, FILENAME_UNCHANGED, PatchAction.UNCHANGED),
                        patchItemWith(PatchFileItem.class, FILENAME_NEW, PatchAction.ADD),
                        patchItemWith(PatchFileItem.class, FILENAME_REMOVED_FROM_SOURCE, PatchAction.REMOVE),
                        patchItemWith(PatchFileItem.class, FILENAME_MODIFIED_FROM_SOURCE, PatchAction.BSDIFF),
                        patchItemWith(PatchFileItem.class, FILENAME_MODIFIED_FROM_INITIAL, PatchAction.BSDIFF_FROM_INITIAL_BASELINE),
                        patchItemWith(PatchDirectoryItem.class, DIRECTORY_UNCHANGED, PatchAction.DELTA),
                        patchItemWith(PatchDirectoryItem.class, DIRECTORY_NEW, PatchAction.ADD),
                        patchItemWith(PatchDirectoryItem.class, DIRECTORY_REMOVED, PatchAction.REMOVE),
                        patchItemWith(PatchDirectoryItem.class, DIRECTORY_ZIP, PatchAction.DELTA)
                ))
        );

        // 2 files bsdiff'ed on top level and 2 files inside DIRECTORY_ZIP
        verify(bsdiff4Service, times(4)).createPatch(any(), any(), any());
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

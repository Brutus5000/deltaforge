package net.brutus5000.deltaforge.patching;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;

public class CompareTaskV1Test {
    private final static String FILENAME_UNCHANGED = "unchanged.txt";
    private final static String FILENAME_NEW = "new.txt";
    private final static String FILENAME_REMOVED_FROM_SOURCE = "removed-from-source.txt";
    private final static String FILENAME_MODIFIED_FROM_SOURCE = "modified-from-source.txt";
    private final static String FILENAME_MODIFIED_FROM_INITIAL = "modified-from-initial.txt";
    private final static String DIRECTORY_UNCHANGED = "unchanged-folder";
    private final static String DIRECTORY_NEW = "new-folder";
    private final static String DIRECTORY_REMOVED = "removed-folder";

    CompareTaskV1 instance;


    @BeforeEach
    void beforeEach() throws Exception {
        String FILE_PREFIX = "src/test/resources/";
        String PATCH_NAME = "source_to_target";
        Path patchFolder = ResourceUtils.getFile(FILE_PREFIX + "testRepo/temp/patching/" + PATCH_NAME).toPath();
        instance = new CompareTaskV1(
                ResourceUtils.getFile(FILE_PREFIX + "testRepo/tags/source").toPath(),
                ResourceUtils.getFile(FILE_PREFIX + "testRepo/tags/initialBaseline").toPath(),
                ResourceUtils.getFile(FILE_PREFIX + "testRepo/tags/target").toPath(),
                patchFolder
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
                    () -> assertEquals("0xfeb06a79", fileItem.getTargetCrc())
            );
        }

        @Test
        void compareFileModifiedFromInitial() throws Exception {
            PatchFileItem fileItem = instance.compareFile(Paths.get(FILENAME_MODIFIED_FROM_INITIAL));

            assertAll("fileItem",
                    () -> assertEquals(FILENAME_MODIFIED_FROM_INITIAL, fileItem.getName()),
                    () -> assertEquals(PatchAction.BSDIFF_FROM_INITIAL_BASELINE, fileItem.getAction()),
                    () -> assertEquals("0xffc5f2e9", fileItem.getBaseCrc()),
                    () -> assertEquals("0x110cdad1", fileItem.getTargetCrc())
            );
        }
    }

    @Nested
    class DirectoryComparison {
        @Test
        void compareDirectories() throws Exception {
            PatchDirectoryItem directoryItem = instance.compareDirectory(Paths.get(""));

            assertThat(directoryItem.getItems(), containsInAnyOrder(
                    patchItemWith(PatchFileItem.class, FILENAME_UNCHANGED, PatchAction.UNCHANGED),
                    patchItemWith(PatchFileItem.class, FILENAME_NEW, PatchAction.ADD),
                    patchItemWith(PatchFileItem.class, FILENAME_REMOVED_FROM_SOURCE, PatchAction.REMOVE),
                    patchItemWith(PatchFileItem.class, FILENAME_MODIFIED_FROM_SOURCE, PatchAction.BSDIFF),
                    patchItemWith(PatchFileItem.class, FILENAME_MODIFIED_FROM_INITIAL, PatchAction.BSDIFF_FROM_INITIAL_BASELINE),
                    patchItemWith(PatchDirectoryItem.class, DIRECTORY_UNCHANGED, PatchAction.DELTA),
                    patchItemWith(PatchDirectoryItem.class, DIRECTORY_NEW, PatchAction.ADD),
                    patchItemWith(PatchDirectoryItem.class, DIRECTORY_REMOVED, PatchAction.REMOVE)
            ));
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
                        patchItemWith(PatchDirectoryItem.class, DIRECTORY_REMOVED, PatchAction.REMOVE)
                ))
        );
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

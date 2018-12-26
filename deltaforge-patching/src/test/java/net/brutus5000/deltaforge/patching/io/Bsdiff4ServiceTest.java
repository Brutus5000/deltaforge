package net.brutus5000.deltaforge.patching.io;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class Bsdiff4ServiceTest {
    private static final String FILE_PREFIX = "./src/test/resources/";

    private Bsdiff4Service instance;

    @BeforeEach
    void beforeEach() {
        instance = new Bsdiff4Service();
    }

    @Test
    void testCreatePatch() throws Exception {
        Path source = Paths.get(FILE_PREFIX + "testRepo/tags/source/modified-from-source.txt");
        Path target = Paths.get(FILE_PREFIX + "testRepo/tags/target/modified-from-source.txt");
        Path patch = Paths.get(FILE_PREFIX + "testRepo/temp/modified-from-source.txt");
        Path patchReference = Paths.get(FILE_PREFIX + "testRepo/patches/modified-from-source.txt");

        try {
            Files.createDirectories(patch.getParent());
            instance.createPatch(source, target, patch);

            assertThat(FileUtils.contentEquals(patch.toFile(), patchReference.toFile()), is(true));
        } finally {
            Files.deleteIfExists(patch);
        }

    }

    @Test
    void testApplyPatch() throws Exception {
        Path source = Paths.get(FILE_PREFIX + "testRepo/tags/source/modified-from-source.txt");
        Path target = Paths.get(FILE_PREFIX + "testRepo/temp/modified-from-source.txt");
        Path patch = Paths.get(FILE_PREFIX + "testRepo/patches/modified-from-source.txt");
        Path targetReference = Paths.get(FILE_PREFIX + "testRepo/tags/target/modified-from-source.txt");

        try {
            Files.createDirectories(patch.getParent());
            instance.applyPatch(source, target, patch);

            assertThat(FileUtils.contentEquals(target.toFile(), targetReference.toFile()), is(true));
        } finally {
            Files.deleteIfExists(target);
        }
    }
}

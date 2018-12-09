package net.brutus5000.deltaforge.patching;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class Bsdiff4ServiceTest {
    Bsdiff4Service instance;

    @BeforeEach
    void beforeEach() {
        instance = new Bsdiff4Service();
    }

    @Test
    void testCreatePatch() throws Exception {
        String FILE_PREFIX = "./src/test/resources/";
        Path source = Paths.get(FILE_PREFIX + "testRepo/tags/source/modified-from-source.txt");
        Path target = Paths.get(FILE_PREFIX + "testRepo/tags/target/modified-from-source.txt");
        Path patch = Paths.get(FILE_PREFIX + "testRepo/temp/modified-from-source.txt");

        try {
            Files.createDirectories(patch.getParent());
            instance.createPatch(source, target, patch);
        } finally {
            Files.deleteIfExists(patch);
        }
    }
}

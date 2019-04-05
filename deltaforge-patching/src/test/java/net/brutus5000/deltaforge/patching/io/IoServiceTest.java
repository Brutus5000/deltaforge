package net.brutus5000.deltaforge.patching.io;


import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

class IoServiceTest {
    public static final String FILE_PREFIX = "./src/test/resources/testArchiveFiles";

    private IoService instance;

    @BeforeEach
    void beforeEach() {
        instance = new IoService();
    }

    @Test
    void crc32() throws Exception {
        String crc32 = instance.crc32(Paths.get(FILE_PREFIX, "archive.zip"));

        assertThat(crc32, is("0x732b472a"));
    }

    @Test
    void getDirectoryItems() throws Exception {
        Collection<Path> directoryItems = instance.getDirectoryItems(Paths.get(FILE_PREFIX));

        assertThat(directoryItems, containsInAnyOrder(
                Paths.get(FILE_PREFIX, "archive.zip"),
                Paths.get(FILE_PREFIX, "archive.7z")
        ));
    }

    @Test
    void determineArchiveType() throws Exception {
        assertAll("detect archive type",
                () -> assertThat(instance.determineArchiveType(Paths.get(FILE_PREFIX, "archive.zip")), is(ArchiveStreamFactory.ZIP)),
                () -> assertThat(instance.determineArchiveType(Paths.get(FILE_PREFIX, "archive.7z")), is(nullValue()))
        );
    }
}

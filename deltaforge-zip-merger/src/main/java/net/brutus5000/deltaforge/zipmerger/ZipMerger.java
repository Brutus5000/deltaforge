package net.brutus5000.deltaforge.zipmerger;

import net.brutus5000.deltaforge.patching.io.IoService;
import net.brutus5000.deltaforge.patching.io.Unzipper;
import net.brutus5000.deltaforge.patching.io.Zipper;
import org.apache.commons.compress.archivers.ArchiveException;

import java.io.IOException;
import java.nio.file.Path;

public class ZipMerger {
    private final IoService ioService;

    public ZipMerger(IoService ioService) {
        this.ioService = ioService;
    }

    public void merge(Path folder, Path zipFile, String archiveType) throws IOException, ArchiveException {
        Path tempDirectory = ioService.createTempDirectory("deltaforge_zip_merge_");
        Unzipper.from(zipFile, archiveType).to(tempDirectory).unzip();

        ioService.copyDirectory(folder, tempDirectory);

        ioService.deleteQuietly(zipFile);

        Zipper.contentOf(tempDirectory, archiveType).to(zipFile).zip();
    }
}

package net.brutus5000.deltaforge.patching.io;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.CREATE;

@Slf4j
public final class Unzipper {
    private final ArchiveInputStream archiveInputStream;
    private final boolean closeStream;
    private Path targetDirectory;

    private Unzipper(ArchiveInputStream archiveInputStream, boolean closeStream) {
        this.archiveInputStream = archiveInputStream;
        this.closeStream = closeStream;
    }

    public static Unzipper from(Path zipFile, String archiveType) throws IOException, ArchiveException {
        ArchiveInputStream archiveInputStream = new ArchiveStreamFactory().createArchiveInputStream(archiveType,
                new BufferedInputStream(Files.newInputStream(zipFile)));

        return new Unzipper(archiveInputStream, true);
    }

    public static Unzipper from(ArchiveInputStream archiveInputStream) {
        return new Unzipper(archiveInputStream, false);
    }

    public Unzipper to(Path targetDirectory) {
        this.targetDirectory = targetDirectory;
        return this;
    }

    public void unzip() throws IOException {
        ArchiveEntry archiveEntry;
        try {
            while ((archiveEntry = archiveInputStream.getNextEntry()) != null) {
                Path targetFile = targetDirectory.resolve(archiveEntry.getName());
                if (archiveEntry.isDirectory()) {
                    log.trace("Creating directory {}", targetFile);
                    Files.createDirectories(targetFile);
                    continue;
                }

                Path parentDirectory = targetFile.getParent();
                if (Files.notExists(parentDirectory)) {
                    log.trace("Creating directory {}", parentDirectory);
                    Files.createDirectories(parentDirectory);
                }

                log.trace("Writing file {}", targetFile);
                try (OutputStream outputStream = Files.newOutputStream(targetFile, CREATE)) {
                    IOUtils.copy(archiveInputStream, outputStream);
                }
            }
        } finally {
            if (closeStream) {
                archiveInputStream.close();
            }
        }
    }
}
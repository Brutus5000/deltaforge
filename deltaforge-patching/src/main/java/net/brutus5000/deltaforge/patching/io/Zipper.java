package net.brutus5000.deltaforge.patching.io;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.utils.IOUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

@Slf4j
public final class Zipper {
    private static final char PATH_SEPARATOR = File.separatorChar;
    private static final Path NEUTRAL_PATH = Paths.get("");

    private final Path directoryToZip;
    private String archiveType;
    private boolean zipContent;
    private ArchiveOutputStream archiveOutputStream;
    private boolean closeStream;

    /**
     * @param archiveType determines the type of the archive
     * @param zipContent  {@code true} if the contents of the directory should be zipped, {@code false} if the specified
     *                    file (directory) should be zipped directly.
     */
    private Zipper(Path directoryToZip, String archiveType, boolean zipContent) {
        this.directoryToZip = directoryToZip;
        this.archiveType = archiveType;
        this.zipContent = zipContent;
    }

    public static Zipper of(Path path, String archiveType) {
        return new Zipper(path, archiveType, false);
    }

    public static Zipper contentOf(Path path, String archiveType) {
        return new Zipper(path, archiveType, true);
    }

    public Zipper to(ArchiveOutputStream archiveOutputStream) {
        this.archiveOutputStream = archiveOutputStream;
        this.closeStream = false;
        return this;
    }

    public Zipper to(Path path) throws IOException, ArchiveException {
        this.archiveOutputStream = new ArchiveStreamFactory().createArchiveOutputStream(archiveType, new BufferedOutputStream(Files.newOutputStream(path)));
        this.closeStream = true;
        return this;
    }

    public void zip() throws IOException {
        Objects.requireNonNull(archiveOutputStream, "archiveOutputStream must not be null");
        Objects.requireNonNull(directoryToZip, "directoryToZip must not be null");

        Files.walkFileTree(directoryToZip, new SimpleFileVisitor<Path>() {
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path relativePath = zipContent
                        ? directoryToZip.relativize(dir)
                        : directoryToZip.getParent().relativize(dir);

                if (relativePath.equals(NEUTRAL_PATH)) {
                    return FileVisitResult.CONTINUE;
                }

                ArchiveEntry archiveEntry = archiveOutputStream.createArchiveEntry(dir.toFile(), relativePath.toString().replace(PATH_SEPARATOR, '/') + '/');
                archiveOutputStream.putArchiveEntry(archiveEntry);
                archiveOutputStream.closeArchiveEntry();
                return FileVisitResult.CONTINUE;
            }

            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                log.trace("Zipping file {}", file.toAbsolutePath());

                Path relativePath;

                if (zipContent) {
                    relativePath = directoryToZip.relativize(file);
                } else {
                    relativePath = directoryToZip.getParent().relativize(file);
                }

                ArchiveEntry archiveEntry = archiveOutputStream.createArchiveEntry(file.toFile(), relativePath.toString().replace(PATH_SEPARATOR, '/'));
                archiveOutputStream.putArchiveEntry(archiveEntry);

                try (InputStream inputStream = Files.newInputStream(file)) {
                    IOUtils.copy(inputStream, archiveOutputStream);
                }

                archiveOutputStream.closeArchiveEntry();
                return FileVisitResult.CONTINUE;
            }
        });

        if (closeStream) {
            archiveOutputStream.close();
        }
    }
}
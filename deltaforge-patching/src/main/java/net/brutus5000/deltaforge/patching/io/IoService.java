package net.brutus5000.deltaforge.patching.io;

import com.google.common.hash.Hashing;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Provides simple file operations as a stateless service.
 * The purpose of this service is to make all file operations mockable in tests.
 */
@Slf4j
public class IoService {

    private final ArchiveStreamFactory archiveStreamFactory = new ArchiveStreamFactory();

    public boolean isDirectory(@NonNull Path path) {
        return Files.isDirectory(path);
    }

    public boolean isFile(@NonNull Path path) {
        return Files.isRegularFile(path);
    }

    public String crc32(@NonNull Path filePath) throws IOException {
        return "0x" + Integer.toHexString(com.google.common.io.Files.asByteSource(filePath.toFile()).hash(Hashing.crc32()).asInt());
    }

    public Path createDirectories(@NonNull Path path) throws IOException {
        return Files.createDirectories(path);
    }

    public Path createTempDirectory(String prefix) throws IOException {
        return Files.createTempDirectory(prefix);
    }

    public void copy(@NonNull Path from, @NonNull Path to) throws IOException {
        Files.createDirectories(to.getParent());
        Files.copy(from, to);
    }

    public void copyDirectory(@NonNull Path from, @NonNull Path to) throws IOException {
        FileUtils.copyDirectory(from.toFile(), to.toFile());
    }

    public void moveDirectory(@NonNull Path from, @NonNull Path to) throws IOException {
        FileUtils.moveDirectory(from.toFile(), to.toFile());
    }

    public void deleteQuietly(@NonNull Path path) {
        FileUtils.deleteQuietly(path.toFile());
    }

    public void deleteDirectory(@NonNull Path path) throws IOException {
        FileUtils.deleteDirectory(path.toFile());
    }

    public Collection<Path> getDirectoryItems(@NonNull Path path) throws IOException {
        return Files.list(path)
                .collect(Collectors.toList());
    }

    /**
     * Detects the archive type of the given file.
     *
     * @param path pointing to any file
     * @return an archive type supported by Apache Comproess or otherwise {@code null} (if unsupported or no archive file)
     * @throws IOException if reading the file fails
     */
    public String determineArchiveType(@NonNull Path path) throws IOException {
        try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(path, StandardOpenOption.READ))) {
            String archiveType = ArchiveStreamFactory.detect(inputStream);

            // If Apache Compress can't write the format, we cannot support it
            // Note: Even though 7z is supported by Apache Compress, it does not work with the streaming approach of our implementation
            if (archiveStreamFactory.getOutputStreamArchiveNames().contains(archiveType)
                    && !archiveType.equals(ArchiveStreamFactory.SEVEN_Z)
            ) {
                return archiveType;
            } else {
                return null;
            }
        } catch (ArchiveException e) {
            return null;
        }
    }

    // TODO: Also support De/compressing of non-archive files (e.g. gz, bz2) using CompressorStreamFactory
    public void zip(@NonNull Path folderPath, @NonNull Path archiveFile, @NonNull String archiveType) throws IOException {
        try {
            Zipper.contentOf(folderPath, archiveType).to(archiveFile).zip();
        } catch (ArchiveException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw new IOException("Archiving failed", e);
            }
        }
    }

    public void unzip(@NonNull Path archiveFile, @NonNull Path folderPath, @NonNull String archiveType) throws IOException {
        try {
            Unzipper.from(archiveFile, archiveType).to(folderPath).unzip();
        } catch (ArchiveException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw new IOException("Extracting archive file failed", e);
            }
        }
    }
}

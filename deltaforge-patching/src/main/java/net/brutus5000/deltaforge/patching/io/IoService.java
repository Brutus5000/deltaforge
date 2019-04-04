package net.brutus5000.deltaforge.patching.io;

import com.google.common.hash.Hashing;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Provides simple file operations as a stateless service.
 * The purpose of this service is to make all file operations mockable in tests.
 */
@Slf4j
public class IoService {
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

    public boolean isZipFile(@NonNull Path path) throws IOException {
        String contentType = Files.probeContentType(path);
        log.trace("Detected content type: " + contentType);

        return Objects.equals(contentType, "application/x-zip-compressed") // Windows
                || Objects.equals(contentType, "application/zip"); // Unix
    }

    public void zip(@NonNull Path folderPath, @NonNull Path archiveFile) throws IOException {
        Zipper.contentOf(folderPath).to(archiveFile);
    }

    public void unzip(@NonNull Path archiveFile, @NonNull Path folderPath) throws IOException {
        Unzipper.from(archiveFile).to(folderPath).unzip();
    }
}

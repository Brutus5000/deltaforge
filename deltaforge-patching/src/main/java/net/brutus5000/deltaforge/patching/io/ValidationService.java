package net.brutus5000.deltaforge.patching.io;

import com.google.common.annotations.VisibleForTesting;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.brutus5000.deltaforge.patching.CrcMismatchException;
import net.brutus5000.deltaforge.patching.meta.validate.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Slf4j
public class ValidationService {
    private final IoService ioService;

    public ValidationService(IoService ioService) {
        this.ioService = ioService;
    }

    public ValidateMetadata buildValidationMetadata(@NonNull String repositoryName, @NonNull String tagName, @NonNull Path tagPath) throws IOException {
        log.debug("buildValidationMetadata for repository `{}`, tag `{}` at path `{}`", repositoryName, tagName, tagPath);

        ValidateDirectoryItem validateDirectoryItem = scanDirectory(tagPath);

        return new ValidateMetadata()
                .setRepository(repositoryName)
                .setTag(tagName)
                .setItems(validateDirectoryItem.getItems());
    }

    @SneakyThrows
    public boolean validateChecksums(@NonNull Path path, @NonNull ValidateMetadata validateMetadata) {
        log.debug("Validate checksums at path `{}` with meta data: {}", path, validateMetadata);
        return validateDirectory(path, validateMetadata.getItems(), false);
    }

    private void validateFile(@NonNull Path filePath, @NonNull ValidateFileItem item) throws IOException {
        log.trace("Validating file: {}", filePath);

        String crc32 = ioService.crc32(filePath);
        if (!Objects.equals(item.getCrc(), crc32)) {
            throw new CrcMismatchException(filePath, item.getCrc(), crc32);
        }
    }

    private boolean validateDirectory(@NonNull Path path, @NonNull Set<ValidateItem> items, boolean rethrowExceptions) throws IOException {
        log.trace("Validating directory: {}", path);

        /*
         Extracting compressed files is an expensive task. Let's do this as late as possible.
         This requires iterating through all items first and processing compressed files at the end.
        */

        List<ValidateFileItem> files = new ArrayList<>();
        List<ValidateDirectoryItem> directories = new ArrayList<>();
        List<ValidateCompressedItem> compressedFiles = new ArrayList<>();

        for (ValidateItem item : items) {
            if (item instanceof ValidateFileItem) {
                files.add((ValidateFileItem) item);
            } else if (item instanceof ValidateDirectoryItem) {
                directories.add((ValidateDirectoryItem) item);
            } else if (item instanceof ValidateCompressedItem) {
                compressedFiles.add((ValidateCompressedItem) item);
            } else {
                throw new IllegalStateException("Unexpected type: " + item.getClass());
            }
        }

        try {
            for (ValidateFileItem file : files) {
                validateFile(path.resolve(file.getName()), file);
            }

            for (ValidateDirectoryItem directory : directories) {
                validateDirectory(path.resolve(directory.getName()), directory.getItems(), true);
            }

            for (ValidateCompressedItem compressedFile : compressedFiles) {
                validateCompressedItem(path.resolve(compressedFile.getName()), compressedFile);
            }
        } catch (IOException e) {
            if (rethrowExceptions) {
                throw e;
            }

            log.debug("Validation failed!", e);
            return false;
        }

        return true;
    }

    private void validateCompressedItem(@NonNull Path path, @NonNull ValidateCompressedItem item) throws IOException {
        log.trace("Validating compressed file: {}", path);

        Path tempDirectory = ioService.createTempDirectory("deltaforge_validate_");
        try {
            ioService.unzip(path, tempDirectory);
            validateDirectory(tempDirectory, item.getItems(), true);
        } finally {
            ioService.deleteQuietly(tempDirectory);
        }
    }

    @VisibleForTesting
    ValidateDirectoryItem scanDirectory(@NonNull Path path) throws IOException {
        log.debug("scanDirectory for `{}`", path);

        Set<ValidateItem> items = new HashSet<>();

        for (Path directoryItem : ioService.getDirectoryItems(path)) {
            if (ioService.isFile(directoryItem)) {
                if (ioService.isZipFile(directoryItem)) {
                    items.add(scanCompressedFile(directoryItem));
                } else {
                    items.add(scanFile(directoryItem));
                }
            } else {
                items.add(scanDirectory(directoryItem));
            }
        }

        return new ValidateDirectoryItem()
                .setName(String.valueOf(path.getFileName()))
                .setItems(items);
    }

    @VisibleForTesting
    ValidateFileItem scanFile(@NonNull Path path) throws IOException {
        log.debug("scanFile for `{}`", path);

        return new ValidateFileItem()
                .setName(String.valueOf(path.getFileName()))
                .setCrc(ioService.crc32(path));
    }

    @VisibleForTesting
    ValidateCompressedItem scanCompressedFile(@NonNull Path path) throws IOException {
        log.debug("scanCompressedFile for `{}`", path);

        Path contentPath = ioService.createTempDirectory("deltaforge_validate_");
        log.debug("temporary directory created at: {}", contentPath);

        ValidateDirectoryItem directoryItem = scanDirectory(contentPath);

        log.debug("deleting temporary directory: {}", contentPath);
        ioService.deleteQuietly(contentPath);

        return new ValidateCompressedItem()
                .setName(String.valueOf(path.getFileName()))
                .setAlgorithm("zip")
                .setItems(directoryItem.getItems());
    }
}

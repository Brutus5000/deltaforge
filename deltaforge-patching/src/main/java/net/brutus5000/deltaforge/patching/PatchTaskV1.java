package net.brutus5000.deltaforge.patching;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.brutus5000.deltaforge.patching.io.Bsdiff4Service;
import net.brutus5000.deltaforge.patching.io.IoService;
import net.brutus5000.deltaforge.patching.meta.patch.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Slf4j
public class PatchTaskV1 {
    private final Bsdiff4Service bsdiff4Service;
    private final IoService ioService;
    private final Path rootSourceFolder;
    private final Path rootInitialBaselineFolder;
    private final Path rootTargetFolder;
    private final Path rootPatchFolder;
    private final String repositoryName;

    private String fromTagName;
    private String toTagName;

    public PatchTaskV1(Bsdiff4Service bsdiff4Service, IoService ioService, Path sourceFolder, Path initialBaselineFolder,
                       Path targetFolder, Path patchFolder, String repositoryName) {
        this.bsdiff4Service = bsdiff4Service;
        this.ioService = ioService;
        this.rootSourceFolder = sourceFolder;
        this.rootInitialBaselineFolder = initialBaselineFolder;
        this.rootTargetFolder = targetFolder;
        this.rootPatchFolder = patchFolder;
        this.repositoryName = repositoryName;
    }

    public PatchMetadata compare(String fromTagName, String toTagName) throws IOException {
        this.fromTagName = fromTagName;
        this.toTagName = toTagName;

        Path of = Paths.get(".");
        PatchDirectoryItem root = compareDirectory(of);

        return new PatchMetadata()
                .setRepository(repositoryName)
                .setFromTag(fromTagName)
                .setToTag(toTagName)
                .setProtocol(1)
                .setItems(root.getItems());
    }

    PatchFileItem compareFile(@NonNull Path relativeFilePath) throws IOException {
        log.debug("compareFile for `{}`", relativeFilePath);

        final PathBuilder paths = new PathBuilder(relativeFilePath);

        PatchFileItem item = new PatchFileItem()
                .setName(relativeFilePath.getFileName().toString());

        // if target folder already exists it's either new or delta
        if (ioService.isFile(paths.target)) {
            item.setTargetCrc(ioService.crc32(paths.target));

            if (ioService.isFile(paths.source)) {
                item.setBaseCrc(ioService.crc32(paths.source));

                if (Objects.equals(item.getBaseCrc(), item.getTargetCrc())) {
                    item.setAction(PatchAction.UNCHANGED);
                } else {
                    item.setAction(PatchAction.BSDIFF);
                    bsdiff4Service.createPatch(paths.source, paths.target, paths.patch);
                }
            } else if (ioService.isFile(paths.initialBaseline)) {
                item.setBaseCrc(ioService.crc32(paths.initialBaseline));

                if (Objects.equals(item.getBaseCrc(), item.getTargetCrc())) {
                    item.setAction(PatchAction.ADD);
                    ioService.copy(paths.target, paths.patch);
                } else {
                    item.setAction(PatchAction.BSDIFF_FROM_INITIAL_BASELINE);
                    bsdiff4Service.createPatch(paths.initialBaseline, paths.target, paths.patch);
                }
            } else {
                item.setAction(PatchAction.ADD);
                ioService.copy(paths.target, paths.patch);
            }
        } else {
            item.setAction(PatchAction.REMOVE);
        }

        return item;
    }

    PatchDirectoryItem compareDirectory(@NonNull Path relativeFolderPath) throws IOException {
        log.debug("compareDirectory for `{}`", relativeFolderPath);

        final PathBuilder paths = new PathBuilder(relativeFolderPath);
        
        PatchAction patchAction;
        Set<Path> subDirectories = new HashSet<>();
        Set<Path> files = new HashSet<>();

        // if target folder already exists it's either new or delta
        if (ioService.isDirectory(paths.target)) {
            scanDirectory(paths.target, subDirectories, files);

            if (ioService.isDirectory(paths.source)) {
                patchAction = PatchAction.DELTA;
                ioService.createDirectories(paths.patch);
                scanDirectory(paths.source, subDirectories, files);
            } else if (ioService.isDirectory(paths.initialBaseline)) {
                patchAction = PatchAction.DELTA;
                ioService.createDirectories(paths.patch);
                scanDirectory(paths.initialBaseline, subDirectories, files);
            } else {
                patchAction = PatchAction.ADD;
            }
        } else {
            patchAction = PatchAction.REMOVE;
        }

        PatchDirectoryItem item = new PatchDirectoryItem()
                .setName(relativeFolderPath.getFileName().toString())
                .setAction(patchAction)
                .setItems(new HashSet<>());

        if (item.getAction() == PatchAction.DELTA) {
            for (Path subDirectory : subDirectories) {
                PatchDirectoryItem subDirectoryItem = compareDirectory(relativeFolderPath.resolve(subDirectory));
                item.getItems().add(subDirectoryItem);
            }

            for (Path file : files) {
                Path relativeFilePath = relativeFolderPath.resolve(file);
                String archiveType = determineArchiveType(relativeFilePath);

                PatchItem fileItem;
                if (archiveType != null) {
                    fileItem = compareCompressedFile(relativeFilePath, archiveType);
                } else {
                    fileItem = compareFile(relativeFilePath);
                }
                item.getItems().add(fileItem);
            }
        }

        return item;
    }

    private String determineArchiveType(@NonNull Path relativeFilePath) throws IOException {
        final PathBuilder paths = new PathBuilder(relativeFilePath);

        String archiveType = null;

        if (ioService.isFile(paths.target)) {
            archiveType = ioService.determineArchiveType(paths.target);
        } else if (ioService.isFile(paths.source)) {
            archiveType = ioService.determineArchiveType(paths.source);
        } else if (ioService.isFile(paths.initialBaseline)) {
            archiveType = ioService.determineArchiveType(paths.initialBaseline);
        }

        log.debug("ArchiveType of `{}` is: {}", relativeFilePath, archiveType);

        return archiveType;
    }

    private void scanDirectory(@NonNull Path directory, @NonNull Set<Path> subDirectories, @NonNull Set<Path> files) throws IOException {
        for (Path path : ioService.getDirectoryItems(directory)) {
            scanDirectoryElement(path, subDirectories, files);
        }
    }

    private void scanDirectoryElement(@NonNull Path folderElement, @NonNull Set<Path> directories, @NonNull Set<Path> files) throws IOException {
        if (ioService.isDirectory(folderElement)) {
            directories.add(folderElement.getFileName());
        } else if (ioService.isFile(folderElement)) {
            files.add(folderElement.getFileName());
        } else {
            throw new IOException(String.format("Given folderElement is neither directory nor file: %s", folderElement.toString()));
        }
    }

    PatchCompressedItem compareCompressedFile(@NonNull Path relativeFilePath, String archiveType) throws IOException {
        final PathBuilder paths = new PathBuilder(relativeFilePath);
        
        PatchCompressedItem item = new PatchCompressedItem()
                .setName(relativeFilePath.getFileName().toString())
                .setAlgorithm(archiveType);

        // if target folder already exists it's either new or delta
        if (ioService.isFile(paths.target)) {

            if (ioService.isFile(paths.source)) {
                if (Objects.equals(ioService.crc32(paths.source), ioService.crc32(paths.target))) {
                    item.setAction(PatchAction.UNCHANGED);
                } else {
                    internalCompareArchive(paths.source, paths.target, paths.patch, item, archiveType, true);
                }
            } else if (ioService.isFile(paths.initialBaseline)) {
                if (Objects.equals(ioService.crc32(paths.initialBaseline), ioService.crc32(paths.target))) {
                    item.setAction(PatchAction.ADD);
                    ioService.copy(paths.target, paths.patch);
                } else {
                    internalCompareArchive(paths.initialBaseline, paths.target, paths.patch, item, archiveType, false);
                }
            } else {
                item.setAction(PatchAction.ADD);
                ioService.copy(paths.target, paths.patch);
            }
        } else {
            item.setAction(PatchAction.REMOVE);
        }

        return item;
    }

    private void internalCompareArchive(@NonNull Path sourceArchive, @NonNull Path targetArchive,
                                        @NonNull Path patchFolder, @NonNull PatchCompressedItem item,
                                        String archiveType, boolean fromSource) throws IOException {
        item.setAction(PatchAction.COMPRESSED_FILE);

        Path targetTemp = ioService.createTempDirectory("deltaforge_target_");
        Path sourceTemp = ioService.createTempDirectory("deltaforge_source_");
        Path baselineTemp = ioService.createTempDirectory("deltaforge_baseline_");

        ioService.unzip(targetArchive, targetTemp, archiveType);
        if (fromSource) {
            ioService.unzip(sourceArchive, sourceTemp, archiveType);
        } else {
            ioService.unzip(sourceArchive, baselineTemp, archiveType);
        }

        PatchTaskV1 compareTask = new PatchTaskV1(bsdiff4Service, ioService, sourceTemp, baselineTemp, targetTemp, patchFolder, repositoryName);
        PatchMetadata patchMetadata = compareTask.compare(fromTagName, toTagName);
        item.setItems(patchMetadata.getItems());

        ioService.deleteQuietly(sourceTemp);
        ioService.deleteQuietly(targetTemp);
        ioService.deleteQuietly(baselineTemp);
    }

    public void apply(@NonNull PatchMetadata patchMetadata) throws IOException {
        applyItems(patchMetadata.getItems(), Paths.get("."));
    }

    void applyItems(@NonNull Set<PatchItem> items, @NonNull Path relativeFolderPath) throws IOException {

        for (PatchItem item : items) {
            if (item instanceof PatchFileItem) {
                applyFile((PatchFileItem) item, relativeFolderPath);
            } else if (item instanceof PatchDirectoryItem) {
                applyDirectory((PatchDirectoryItem) item, relativeFolderPath);
            } else if (item instanceof PatchCompressedItem) {
                applyZipFile((PatchCompressedItem) item, relativeFolderPath);
            }
        }
    }

    private void verifyCrc(@NonNull Path filePath, String expectedCrc) throws IOException {
        String actualCrc = ioService.crc32(filePath);

        if (!Objects.equals(actualCrc, expectedCrc)) {
            throw new CrcMismatchException(filePath, expectedCrc, actualCrc);
        }
    }

    void applyFile(@NonNull PatchFileItem fileItem, @NonNull Path relativeFolderPath) throws IOException {
        log.debug("apply PatchFileItem {} in relative path {}", fileItem);

        final PathBuilder paths = new PathBuilder(relativeFolderPath, fileItem);
        
        switch (fileItem.getAction()) {
            case UNCHANGED:
                verifyCrc(paths.source, fileItem.getBaseCrc());
                ioService.copy(paths.source, paths.target);
                break;
            case ADD:
                ioService.copy(paths.patch, paths.target);
                verifyCrc(paths.target, fileItem.getTargetCrc());
                break;
            case REMOVE:
                // do nothing - the file will not be copied to the target path
                break;
            case BSDIFF:
                verifyCrc(paths.source, fileItem.getBaseCrc());
                bsdiff4Service.applyPatch(paths.source, paths.target, paths.patch);
                verifyCrc(paths.target, fileItem.getTargetCrc());
                break;
            case BSDIFF_FROM_INITIAL_BASELINE:
                verifyCrc(paths.initialBaseline, fileItem.getBaseCrc());
                bsdiff4Service.applyPatch(paths.initialBaseline, paths.target, paths.patch);
                verifyCrc(paths.target, fileItem.getTargetCrc());
                break;
            default:
                throw new IllegalStateException("Action type is undefined for applying files: " + fileItem.getAction());
        }
    }

    void applyDirectory(@NonNull PatchDirectoryItem directoryItem, @NonNull Path relativeFolderPath) throws IOException {
        log.debug("apply PatchDirectoryItem {} in relative path {}", directoryItem);

        final PathBuilder paths = new PathBuilder(relativeFolderPath, directoryItem);

        switch (directoryItem.getAction()) {
            case ADD:
                ioService.copyDirectory(paths.patch, paths.target);
                break;
            case REMOVE:
                // do nothing - the folder will not be copied to the target path
                break;
            case DELTA:
                applyItems(directoryItem.getItems(), relativeFolderPath.resolve(directoryItem.getName()));
                break;
            default:
                throw new IllegalStateException("Action type is undefined for applying directories: " + directoryItem.getAction());
        }
    }

    void applyZipFile(@NonNull PatchCompressedItem compressedItem, @NonNull Path relativeFolderPath) throws IOException {
        log.debug("apply PatchCompressedItem {} in relative path {}", compressedItem);

        final PathBuilder paths = new PathBuilder(relativeFolderPath, compressedItem);

        switch (compressedItem.getAction()) {
            case UNCHANGED:
                ioService.copy(paths.source, paths.target);
                break;
            case ADD:
                ioService.copy(paths.patch, paths.target);
                break;
            case REMOVE:
                // do nothing - the file will not be copied to the target path
                break;
            case DELTA:
                Path tempRootDirectory = ioService.createTempDirectory("deltaforge_zip_root_");
                Path sourceFolder = ioService.createDirectories(tempRootDirectory.resolve("source"));
                Path targetFolder = ioService.createDirectories(tempRootDirectory.resolve("target"));
                Path initialBaselineFolder = ioService.createDirectories(tempRootDirectory.resolve("initialBaseline"));
                Path patchFolder = ioService.createDirectories(tempRootDirectory.resolve("patch"));

                ioService.unzip(paths.source, sourceFolder, compressedItem.getAlgorithm());
                ioService.unzip(paths.patch, patchFolder, compressedItem.getAlgorithm());

                if (compressedItem.requiresInitialBaseline()) {
                    ioService.unzip(paths.initialBaseline, initialBaselineFolder, compressedItem.getAlgorithm());
                }

                PatchTaskV1 zipCompareTask = new PatchTaskV1(bsdiff4Service, ioService, sourceFolder, initialBaselineFolder, targetFolder, patchFolder, repositoryName);

                zipCompareTask.applyItems(compressedItem.getItems(), Paths.get("."));

                ioService.zip(targetFolder, paths.target, compressedItem.getAlgorithm());
                ioService.deleteDirectory(tempRootDirectory);

                break;
            default:
                throw new IllegalStateException("Action type is undefined for applying zip files: " + compressedItem.getAction());
        }

    }

    private class PathBuilder {
        public final Path source;
        public final Path target;
        public final Path initialBaseline;
        public final Path patch;

        public PathBuilder(Path relativePath) {
            this.source = rootSourceFolder.resolve(relativePath);
            this.target = rootTargetFolder.resolve(relativePath);
            this.initialBaseline = rootInitialBaselineFolder.resolve(relativePath);
            this.patch = rootPatchFolder.resolve(relativePath);
        }

        public PathBuilder(Path relativePath, PatchItem patchItem) {
            this(relativePath.resolve(patchItem.getName()));
        }
    }
}

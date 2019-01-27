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
public class CompareTaskV1 {
    private final Bsdiff4Service bsdiff4Service;
    private final IoService ioService;
    private final Path rootSourceFolder;
    private final Path rootInitialBaselineFolder;
    private final Path rootTargetFolder;
    private final Path rootPatchFolder;
    private final String repositoryName;
    private final String fromTagName;
    private final String toTagName;

    public CompareTaskV1(Bsdiff4Service bsdiff4Service, IoService ioService, Path sourceFolder, Path initialBaselineFolder,
                         Path targetFolder, Path patchFolder, String repositoryName, String fromTagName,
                         String toTagName) {
        this.bsdiff4Service = bsdiff4Service;
        this.ioService = ioService;
        this.rootSourceFolder = sourceFolder;
        this.rootInitialBaselineFolder = initialBaselineFolder;
        this.rootTargetFolder = targetFolder;
        this.rootPatchFolder = patchFolder;
        this.repositoryName = repositoryName;
        this.fromTagName = fromTagName;
        this.toTagName = toTagName;
    }

    public PatchMetadata compare() throws IOException {
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
        final Path sourceFile = rootSourceFolder.resolve(relativeFilePath);
        final Path targetFile = rootTargetFolder.resolve(relativeFilePath);
        final Path initialBaselineFile = rootInitialBaselineFolder.resolve(relativeFilePath);
        final Path patchFile = rootPatchFolder.resolve(relativeFilePath);

        PatchFileItem item = new PatchFileItem()
                .setName(relativeFilePath.getFileName().toString());

        // if target folder already exists it's either new or delta
        if (ioService.isFile(targetFile)) {
            item.setTargetCrc(ioService.crc32(targetFile));

            if (ioService.isFile(sourceFile)) {
                item.setBaseCrc(ioService.crc32(sourceFile));

                if (Objects.equals(item.getBaseCrc(), item.getTargetCrc())) {
                    item.setAction(PatchAction.UNCHANGED);
                } else {
                    item.setAction(PatchAction.BSDIFF);
                    bsdiff4Service.createPatch(sourceFile, targetFile, patchFile);
                }
            } else if (ioService.isFile(initialBaselineFile)) {
                item.setBaseCrc(ioService.crc32(initialBaselineFile));

                if (Objects.equals(item.getBaseCrc(), item.getTargetCrc())) {
                    item.setAction(PatchAction.UNCHANGED);
                } else {
                    item.setAction(PatchAction.BSDIFF_FROM_INITIAL_BASELINE);
                    bsdiff4Service.createPatch(initialBaselineFile, targetFile, patchFile);
                }
            } else {
                item.setAction(PatchAction.ADD);
            }
        } else {
            item.setAction(PatchAction.REMOVE);
        }

        return item;
    }

    PatchDirectoryItem compareDirectory(@NonNull Path relativeFolderPath) throws IOException {
        log.debug("compareDirectory for `{}`", relativeFolderPath);

        final Path sourceFolder = rootSourceFolder.resolve(relativeFolderPath);
        final Path targetFolder = rootTargetFolder.resolve(relativeFolderPath);
        final Path initialBaselineFolder = rootInitialBaselineFolder.resolve(relativeFolderPath);
        final Path patchFolder = rootPatchFolder.resolve(relativeFolderPath);

        PatchAction patchAction;
        Set<Path> subDirectories = new HashSet<>();
        Set<Path> files = new HashSet<>();

        // if target folder already exists it's either new or delta
        if (ioService.isDirectory(targetFolder)) {
            scanDirectory(targetFolder, subDirectories, files);

            if (ioService.isDirectory(sourceFolder)) {
                patchAction = PatchAction.DELTA;
                ioService.createDirectories(patchFolder);
                scanDirectory(sourceFolder, subDirectories, files);
            } else if (ioService.isDirectory(initialBaselineFolder)) {
                patchAction = PatchAction.DELTA;
                ioService.createDirectories(patchFolder);
                scanDirectory(initialBaselineFolder, subDirectories, files);
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

        for (Path subDirectory : subDirectories) {
            PatchDirectoryItem subDirectoryItem = compareDirectory(relativeFolderPath.resolve(subDirectory));
            item.getItems().add(subDirectoryItem);
        }

        for (Path file : files) {
            PatchItem fileItem;
            if (ioService.isZipFile(file)) {
                fileItem = compareZipFile(relativeFolderPath.resolve(file));
            } else {
                fileItem = compareFile(relativeFolderPath.resolve(file));
            }
            item.getItems().add(fileItem);
        }

        return item;
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

    PatchCompressedItem compareZipFile(@NonNull Path relativeFilePath) throws IOException {
        final Path sourceFolder = rootSourceFolder.resolve(relativeFilePath);
        final Path targetFolder = rootTargetFolder.resolve(relativeFilePath);
        final Path initialBaselineFolder = rootInitialBaselineFolder.resolve(relativeFilePath);
        final Path patchFolder = rootPatchFolder.resolve(relativeFilePath);


        PatchCompressedItem item = new PatchCompressedItem()
                .setName(relativeFilePath.getFileName().toString())
                .setAlgorithm("zip");

        // if target folder already exists it's either new or delta
        if (ioService.isFile(targetFolder)) {

            if (ioService.isFile(sourceFolder)) {
                if (Objects.equals(ioService.crc32(sourceFolder), ioService.crc32(targetFolder))) {
                    item.setAction(PatchAction.UNCHANGED);
                } else {
                    internalCompareArchive(sourceFolder, targetFolder, patchFolder, item, true);
                }
            } else if (ioService.isFile(initialBaselineFolder)) {
                if (Objects.equals(ioService.crc32(initialBaselineFolder), ioService.crc32(targetFolder))) {
                    item.setAction(PatchAction.UNCHANGED);
                } else {
                    internalCompareArchive(initialBaselineFolder, targetFolder, patchFolder, item, false);
                }
            } else {
                item.setAction(PatchAction.ADD);
            }
        } else {
            item.setAction(PatchAction.REMOVE);
        }

        return item;
    }

    private void internalCompareArchive(@NonNull Path sourceArchive, @NonNull Path targetArchive, @NonNull Path patchFolder, @NonNull PatchCompressedItem item, boolean fromSource) throws IOException {
        item.setAction(PatchAction.COMPRESSED_FILE);

        Path targetTemp = ioService.createTempDirectory("deltaforge_target_");
        Path sourceTemp = ioService.createTempDirectory("deltaforge_source_");
        Path baselineTemp = ioService.createTempDirectory("deltaforge_baseline_");

        ioService.unzip(targetArchive, targetTemp);
        if (fromSource) {
            ioService.unzip(sourceArchive, sourceTemp);
        } else {
            ioService.unzip(sourceArchive, baselineTemp);
        }

        CompareTaskV1 compareTask = new CompareTaskV1(bsdiff4Service, ioService, sourceTemp, baselineTemp, targetTemp, patchFolder,
                repositoryName, fromTagName, toTagName);
        PatchMetadata patchMetadata = compareTask.compare();
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

        final Path sourceFile = rootSourceFolder.resolve(relativeFolderPath).resolve(fileItem.getName());
        final Path targetFile = rootTargetFolder.resolve(relativeFolderPath).resolve(fileItem.getName());
        final Path initialBaselineFile = rootInitialBaselineFolder.resolve(relativeFolderPath).resolve(fileItem.getName());
        final Path patchFile = rootPatchFolder.resolve(relativeFolderPath).resolve(fileItem.getName());

        switch (fileItem.getAction()) {
            case UNCHANGED:
                verifyCrc(sourceFile, fileItem.getBaseCrc());
                break;
            case ADD:
                ioService.copy(patchFile, targetFile);
                verifyCrc(targetFile, fileItem.getTargetCrc());
                break;
            case REMOVE:
                ioService.deleteQuietly(targetFile);
                break;
            case BSDIFF:
                verifyCrc(sourceFile, fileItem.getBaseCrc());
                bsdiff4Service.applyPatch(sourceFile, targetFile, patchFile);
                verifyCrc(targetFile, fileItem.getTargetCrc());
                break;
            case BSDIFF_FROM_INITIAL_BASELINE:
                verifyCrc(initialBaselineFile, fileItem.getBaseCrc());
                bsdiff4Service.applyPatch(initialBaselineFile, targetFile, patchFile);
                verifyCrc(targetFile, fileItem.getTargetCrc());
                break;
            default:
                throw new IllegalStateException("Action type is undefined for applying files: " + fileItem.getAction());
        }
    }

    void applyDirectory(@NonNull PatchDirectoryItem directoryItem, @NonNull Path relativeFolderPath) throws IOException {
        log.debug("apply PatchDirectoryItem {} in relative path {}", directoryItem);

        final Path targetFolder = rootTargetFolder.resolve(relativeFolderPath).resolve(directoryItem.getName());
        final Path patchFolder = rootPatchFolder.resolve(relativeFolderPath).resolve(directoryItem.getName());

        switch (directoryItem.getAction()) {
            case ADD:
                ioService.copyDirectory(patchFolder, targetFolder);
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

        final Path tempRootDirectory = ioService.createTempDirectory("deltaforge_zip_root_");
        final Path sourceFolder = ioService.createDirectories(tempRootDirectory.resolve("source"));
        final Path targetFolder = ioService.createDirectories(tempRootDirectory.resolve("target"));
        final Path initialBaselineFolder = ioService.createDirectories(tempRootDirectory.resolve("initialBaseline"));
        final Path patchFolder = ioService.createDirectories(tempRootDirectory.resolve("patch"));

        ioService.unzip(rootSourceFolder.resolve(relativeFolderPath).resolve(compressedItem.getName()), sourceFolder);
        ioService.unzip(rootTargetFolder.resolve(relativeFolderPath).resolve(compressedItem.getName()), targetFolder);
        ioService.unzip(rootPatchFolder.resolve(relativeFolderPath).resolve(compressedItem.getName()), patchFolder);

        if (compressedItem.requiresInitialBaseline()) {
            ioService.unzip(rootInitialBaselineFolder.resolve(relativeFolderPath).resolve(compressedItem.getName()), initialBaselineFolder);
        }

        CompareTaskV1 zipCompareTask = new CompareTaskV1(bsdiff4Service, ioService, sourceFolder, initialBaselineFolder, targetFolder, patchFolder,
                repositoryName, fromTagName, toTagName);

        zipCompareTask.applyItems(compressedItem.getItems(), Paths.get("."));

        ioService.zip(targetFolder, rootTargetFolder.resolve(relativeFolderPath).resolve(compressedItem.getName()));

        ioService.deleteDirectory(tempRootDirectory);
    }
}

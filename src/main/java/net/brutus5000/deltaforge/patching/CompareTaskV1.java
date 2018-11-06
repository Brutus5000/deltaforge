package net.brutus5000.deltaforge.patching;

import com.google.common.hash.Hashing;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class CompareTaskV1 {
    private final Bsdiff4Service bsdiff4Service;
    private final Path rootSourceFolder;
    private final Path rootInitialBaselineFolder;
    private final Path rootTargetFolder;
    private final Path rootPatchFolder;

    public CompareTaskV1(Bsdiff4Service bsdiff4Service, Path sourceFolder, Path initialBaselineFolder, Path targeteFolder, Path patchFolder) {
        this.bsdiff4Service = bsdiff4Service;
        this.rootSourceFolder = sourceFolder;
        this.rootInitialBaselineFolder = initialBaselineFolder;
        this.rootTargetFolder = targeteFolder;
        this.rootPatchFolder = patchFolder;
    }

    public PatchMetadata compare() throws IOException {
        Path of = Paths.get(".");
        PatchDirectoryItem root = compareDirectory(of);

        return new PatchMetadata()
                .setProtocol(1)
                .setItems(root.getItems());
    }

    public PatchFileItem compareFile(@NonNull Path relativeFilePath) throws IOException {
        final Path sourceFile = rootSourceFolder.resolve(relativeFilePath);
        final Path targetFile = rootTargetFolder.resolve(relativeFilePath);
        final Path initialBaselineFile = rootInitialBaselineFolder.resolve(relativeFilePath);
        final Path patchFile = rootPatchFolder.resolve(relativeFilePath);

        PatchFileItem item = new PatchFileItem()
                .setName(relativeFilePath.getFileName().toString());

        // if target folder already exists it's either new or delta
        if (Files.isRegularFile(targetFile)) {
            item.setTargetCrc(crc32(targetFile));

            if (Files.isRegularFile(sourceFile)) {
                item.setBaseCrc(crc32(sourceFile));

                if (Objects.equals(item.getBaseCrc(), item.getTargetCrc())) {
                    item.setAction(PatchAction.UNCHANGED);
                } else {
                    item.setAction(PatchAction.BSDIFF);
                    bsdiff4Service.createPatch(sourceFile, targetFile, patchFile);
                }
            } else if (Files.isRegularFile(initialBaselineFile)) {
                item.setBaseCrc(crc32(initialBaselineFile));

                if (Objects.equals(item.getBaseCrc(), item.getTargetCrc())) {
                    item.setAction(PatchAction.UNCHANGED);
                } else {
                    item.setAction(PatchAction.BSDIFF_FROM_INITIAL_BASELINE);
                    bsdiff4Service.createPatch(sourceFile, initialBaselineFile, patchFile);
                }
            } else {
                item.setAction(PatchAction.ADD);
            }
        } else {
            item.setAction(PatchAction.REMOVE);
        }

        return item;
    }

    private String crc32(@NonNull Path filePath) throws IOException {
        return "0x" + Integer.toHexString(com.google.common.io.Files.asByteSource(filePath.toFile()).hash(Hashing.crc32()).asInt());
    }

    public PatchDirectoryItem compareDirectory(@NonNull Path relativeFolderPath) throws IOException {
        log.debug("compareDirectory for `{}`", relativeFolderPath);

        final Path sourceFolder = rootSourceFolder.resolve(relativeFolderPath);
        final Path targetFolder = rootTargetFolder.resolve(relativeFolderPath);
        final Path initialBaselineFolder = rootInitialBaselineFolder.resolve(relativeFolderPath);
        final Path patchFolder = rootPatchFolder.resolve(relativeFolderPath);

        PatchAction patchAction;
        Set<Path> subDirectories = new HashSet<>();
        Set<Path> files = new HashSet<>();

        // if target folder already exists it's either new or delta
        if (Files.isDirectory(targetFolder)) {
            scanDirectory(targetFolder, subDirectories, files);

            if (Files.isDirectory(sourceFolder) || Files.isDirectory(initialBaselineFolder)) {
                patchAction = PatchAction.DELTA;
                Files.createDirectories(patchFolder);
                scanDirectory(sourceFolder, subDirectories, files);
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
            if (isZipFile(file)) {
                fileItem = compareZipFile(relativeFolderPath.resolve(file));
            } else {
                fileItem = compareFile(relativeFolderPath.resolve(file));
            }
            item.getItems().add(fileItem);
        }

        return item;
    }

    private void scanDirectory(@NonNull Path directory, @NonNull Set<Path> subDirectories, @NonNull Set<Path> files) throws IOException {
        List<Path> folderElements = Files.list(directory)
                .collect(Collectors.toList());

        for (Path path : folderElements) {
            scanDirectoryElement(path, subDirectories, files);
        }
    }

    private void scanDirectoryElement(@NonNull Path folderElement, @NonNull Set<Path> directories, @NonNull Set<Path> files) throws IOException {
        if (Files.isDirectory(folderElement)) {
            directories.add(folderElement.getFileName());
        } else if (Files.isRegularFile(folderElement)) {
            files.add(folderElement.getFileName());
        } else {
            throw new IOException(String.format("Given folderElement is neither directory nor file: %s", folderElement.toString()));
        }
    }

    public boolean isZipFile(@NonNull Path relativeFilePath) throws IOException {
        String contentType = Files.probeContentType(rootTargetFolder.resolve(relativeFilePath));
        log.debug("Detected content type: " + contentType);

        return Objects.equals(contentType, "application/x-zip-compressed");
    }

    public PatchCompressedItem compareZipFile(@NonNull Path relativeFilePath) throws IOException {
        final Path sourceFolder = rootSourceFolder.resolve(relativeFilePath);
        final Path targetFolder = rootTargetFolder.resolve(relativeFilePath);
        final Path initialBaselineFolder = rootInitialBaselineFolder.resolve(relativeFilePath);
        final Path patchFolder = rootPatchFolder.resolve(relativeFilePath);

        Path sourceTemp = Files.createTempDirectory("deltaforge_source_");
        Path targetTemp = Files.createTempDirectory("deltaforge_target_");
        Path baselineTemp = Files.createTempDirectory("deltaforge_baseline_");

        PatchCompressedItem item = new PatchCompressedItem()
                .setName(relativeFilePath.getFileName().toString())
                .setAlgorithm("zip");

        // if target folder already exists it's either new or delta
        if (Files.isRegularFile(targetFolder)) {
            ZipUtils.extractArchiveToFolder(targetFolder, targetTemp);

            if (Files.isRegularFile(sourceFolder)) {
                if (Objects.equals(crc32(sourceFolder), crc32(targetFolder))) {
                    item.setAction(PatchAction.UNCHANGED);
                } else {
                    ZipUtils.extractArchiveToFolder(sourceFolder, sourceTemp);
                    CompareTaskV1 compareTask = new CompareTaskV1(bsdiff4Service, sourceFolder, baselineTemp, targetFolder, patchFolder);
                    PatchMetadata patchMetadata = compareTask.compare();
                    item.setItems(patchMetadata.getItems());
                }
            } else if (Files.isRegularFile(initialBaselineFolder)) {
                if (Objects.equals(crc32(initialBaselineFolder), crc32(targetFolder))) {
                    item.setAction(PatchAction.UNCHANGED);
                } else {
                    ZipUtils.extractArchiveToFolder(sourceFolder, baselineTemp);
                    CompareTaskV1 compareTask = new CompareTaskV1(bsdiff4Service, sourceFolder, baselineTemp, targetFolder, patchFolder);
                    PatchMetadata patchMetadata = compareTask.compare();
                    item.setItems(patchMetadata.getItems());
                }
            } else {
                item.setAction(PatchAction.ADD);
            }
        } else {
            item.setAction(PatchAction.REMOVE);
        }

        FileSystemUtils.deleteRecursively(sourceTemp);
        FileSystemUtils.deleteRecursively(targetTemp);
        FileSystemUtils.deleteRecursively(baselineTemp);

        return item;
    }
}

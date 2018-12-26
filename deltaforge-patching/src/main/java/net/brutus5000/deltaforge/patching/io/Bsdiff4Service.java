package net.brutus5000.deltaforge.patching.io;

import io.sigpipe.jbsdiff.Diff;
import io.sigpipe.jbsdiff.InvalidHeaderException;
import io.sigpipe.jbsdiff.Patch;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.compressors.CompressorException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Slf4j
public class Bsdiff4Service {
    public void createPatch(Path sourceFile, Path targetFile, Path patchFile) throws IOException {
        log.debug("Begin creating patch, sourceFile='{}', targetFile='{}', applyFile='{}'",
                sourceFile, targetFile, patchFile);

        byte[] sourceBytes = Files.readAllBytes(sourceFile);
        byte[] targetBytes = Files.readAllBytes(targetFile);
        @Cleanup OutputStream patchFileOutputStream = Files.newOutputStream(patchFile, StandardOpenOption.CREATE_NEW);
        try {
            Diff.diff(sourceBytes, targetBytes, patchFileOutputStream);
        } catch (CompressorException | InvalidHeaderException e) {
            log.error("Creating patch failed: {}", e.getMessage());
            throw new IOException(e);
        }
    }

    public void applyPatch(Path sourceFile, Path targetFile, Path patchFile) throws IOException {
        log.debug("Begin applying patch, sourceFile='{}', targetFile='{}', applyFile='{}'",
                sourceFile, targetFile, patchFile);

        byte[] sourceBytes = Files.readAllBytes(sourceFile);
        byte[] patchBytes = Files.readAllBytes(patchFile);
        @Cleanup OutputStream patchFileOutputStream = Files.newOutputStream(targetFile, StandardOpenOption.CREATE_NEW);
        try {
            Patch.patch(sourceBytes, patchBytes, patchFileOutputStream);
        } catch (CompressorException | InvalidHeaderException e) {
            log.error("Applying patch failed: {}", e.getMessage());
            throw new IOException(e);
        }
    }
}

package net.brutus5000.deltaforge.patching;

import lombok.Getter;

import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;

@Getter
public class CrcMismatchException extends IOException {
    private final Path filePath;
    private final String expectedCrc;
    private final String actualCrc;

    public CrcMismatchException(Path filePath, String expectedCrc, String actualCrc) {
        super(MessageFormat.format("Mismatch on CRC check in file `{0}` (expected={1}, actual={2})",
                filePath.getFileName(),
                expectedCrc,
                actualCrc));
        this.filePath = filePath;
        this.expectedCrc = expectedCrc;
        this.actualCrc = actualCrc;
    }
}

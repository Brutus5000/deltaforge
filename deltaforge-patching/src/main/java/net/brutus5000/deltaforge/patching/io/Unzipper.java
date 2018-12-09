package net.brutus5000.deltaforge.patching.io;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.nio.file.StandardOpenOption.CREATE;

@Slf4j
public final class Unzipper {
    private final ZipInputStream zipInputStream;
    private final boolean closeStream;
    private ByteCountListener byteCountListener;
    private int byteCountInterval;
    private int bufferSize;
    private long totalBytes;
    private Path targetDirectory;
    private long lastCountUpdate;

    private Unzipper(ZipInputStream zipInputStream, boolean closeStream) {
        this.zipInputStream = zipInputStream;
        this.closeStream = closeStream;
        // 4K
        bufferSize = 0x1000;
        byteCountInterval = 40;
    }

    public static Unzipper from(Path zipFile) throws IOException {
        return new Unzipper(new ZipInputStream(Files.newInputStream(zipFile)), true);
    }

    public static Unzipper from(ZipInputStream zipInputStream) {
        return new Unzipper(zipInputStream, false);
    }

    public Unzipper to(Path targetDirectory) {
        this.targetDirectory = targetDirectory;
        return this;
    }

    public Unzipper byteCountInterval(int byteCountInterval) {
        this.byteCountInterval = byteCountInterval;
        return this;
    }

    public Unzipper listener(ByteCountListener byteCountListener) {
        this.byteCountListener = byteCountListener;
        return this;
    }

    public Unzipper bufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    public Unzipper totalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
        return this;
    }

    public void unzip() throws IOException {
        byte[] buffer = new byte[bufferSize];

        long bytesDone = 0;

        ZipEntry zipEntry;
        try {
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                Path targetFile = targetDirectory.resolve(zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    log.trace("Creating directory {}", targetFile);
                    Files.createDirectories(targetFile);
                    continue;
                }

                Path parentDirectory = targetFile.getParent();
                if (Files.notExists(parentDirectory)) {
                    log.trace("Creating directory {}", parentDirectory);
                    Files.createDirectories(parentDirectory);
                }

                long compressedSize = zipEntry.getCompressedSize();
                if (compressedSize != -1) {
                    bytesDone += compressedSize;
                }

                log.trace("Writing file {}", targetFile);
                try (OutputStream outputStream = Files.newOutputStream(targetFile, CREATE)) {
                    int length;
                    while ((length = zipInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, length);

                        long now = System.currentTimeMillis();
                        if (byteCountListener != null && lastCountUpdate < now - byteCountInterval) {
                            byteCountListener.updateBytesWritten(bytesDone, totalBytes);
                            lastCountUpdate = now;
                        }
                    }
                }
            }
        } finally {
            if (closeStream) {
                zipInputStream.close();
            }
        }
    }
}
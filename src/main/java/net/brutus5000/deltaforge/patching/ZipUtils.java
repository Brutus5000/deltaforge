package net.brutus5000.deltaforge.patching;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.Enumeration;

public class ZipUtils {
    private ZipUtils() {
        // static class
    }

    public static void extractArchiveToFolder(Path archive, Path targetFolder) throws IOException {
        try (ZipFile zipFile = new ZipFile(archive.toFile())) {
            Enumeration<? extends ZipArchiveEntry> entries = zipFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                File entryDestination = new File(targetFolder.toFile(), entry.getName());
                if (entry.isDirectory()) {
                    entryDestination.mkdirs();
                } else {
                    entryDestination.getParentFile().mkdirs();
                    InputStream in = zipFile.getInputStream(entry);
                    OutputStream out = new FileOutputStream(entryDestination);
                    IOUtils.copy(in, out);
                    IOUtils.closeQuietly(in);
                    out.close();
                }
            }
        }
    }
}

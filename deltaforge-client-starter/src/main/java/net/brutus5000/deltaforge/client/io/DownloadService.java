package net.brutus5000.deltaforge.client.io;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

public interface DownloadService {
    /**
     * Downloads the file at the specified URL to the specified target path.
     */
    void download(URL url, Path path) throws IOException, URISyntaxException, InterruptedException;

    /**
     * Reads the file at the specified URL into a byte array.
     */
    String read(URL url) throws DownloadException;
}

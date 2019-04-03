package net.brutus5000.deltaforge.client.io;

import net.brutus5000.deltaforge.client.error.DownloadException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

/**
 * An interface of a download service that is used to download files from the server.
 * <p>
 * Implement your own service if you need more than the {@see SimpleDownloadService} can offer (e.g. download progress).
 */
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

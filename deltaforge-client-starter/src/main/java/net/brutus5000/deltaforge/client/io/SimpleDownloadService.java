package net.brutus5000.deltaforge.client.io;

import net.brutus5000.deltaforge.client.error.DownloadException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

/**
 * A simple download service.
 */
public class SimpleDownloadService implements DownloadService {
    private final HttpClient httpClient;

    public SimpleDownloadService() {
        httpClient = HttpClient.newHttpClient();
    }

    @Override
    public void download(URL url, Path path) throws DownloadException {
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url.toURI())
                    .build();

            httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofFile(path));
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw new DownloadException(e, url);
        }
    }

    @Override
    public String read(URL url) throws DownloadException {
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url.toURI())
                    .build();

            return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw new DownloadException(e, url);
        }
    }
}

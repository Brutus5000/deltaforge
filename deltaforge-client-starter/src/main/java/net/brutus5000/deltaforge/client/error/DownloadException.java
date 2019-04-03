package net.brutus5000.deltaforge.client.error;

import lombok.Getter;

import java.io.IOException;
import java.net.URL;

/**
 * {@code DownloadException} can be thrown on errors download of a file.
 */
@Getter
public class DownloadException extends IOException {
    private final URL url;

    public DownloadException(Throwable cause, URL url) {
        super(cause);
        this.url = url;
    }
}

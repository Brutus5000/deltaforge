package net.brutus5000.deltaforge.client.io;

import lombok.Getter;

import java.io.IOException;
import java.net.URL;

@Getter
public class DownloadException extends IOException {
    private final URL url;

    public DownloadException(Throwable cause, URL url) {
        super(cause);
        this.url = url;
    }
}

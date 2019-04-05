package net.brutus5000.deltaforge.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "deltaforge")
@Validated
@Data
public class DeltaforgeClientProperties {
    /**
     * The url to the Deltaforge server.
     */
    @NotBlank
    private String serverApiUrl;

    /**
     * The url to the content server serving the Deltaforge server files.
     */
    @NotBlank
    private String serverContentUrl;

    /**
     *
     */
    @NotBlank
    private String rootDirectory;
}

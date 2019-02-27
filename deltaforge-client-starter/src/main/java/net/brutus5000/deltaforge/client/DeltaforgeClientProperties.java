package net.brutus5000.deltaforge.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "deltaforge")
@Data
public class DeltaforgeClientProperties {
    @NotBlank
    private String serverUrl;

    @NotBlank
    private String rootDirectory;
}

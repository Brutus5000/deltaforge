package net.brutus5000.deltaforge.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "deltaforge")
@Validated
@Data
public class DeltaforgeClientProperties {
    @NotBlank
    private String serverUrl;

    @NotBlank
    private String rootDirectory;
}

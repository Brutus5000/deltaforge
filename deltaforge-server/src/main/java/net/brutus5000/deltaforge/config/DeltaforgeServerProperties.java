package net.brutus5000.deltaforge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ConfigurationProperties(prefix = "deltaforge", ignoreUnknownFields = false)
public class DeltaforgeServerProperties {
    @NotBlank
    private String rootRepositoryPath;

    @NotNull
    private Long baselineFilesizeThreshold;
}

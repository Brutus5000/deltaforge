package net.brutus5000.deltaforge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotBlank;

@Data
@ConfigurationProperties(prefix = "deltaforge", ignoreUnknownFields = false)
public class DeltaForgeProperties {
    @NotBlank
    private String rootRepositoryPath;
}
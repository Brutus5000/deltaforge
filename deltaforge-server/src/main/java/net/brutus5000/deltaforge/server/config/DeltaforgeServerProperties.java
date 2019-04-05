package net.brutus5000.deltaforge.server.config;

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

    private final Security security = new Security();

    @Data
    public static class Security {
        @NotBlank
        private String authTokenHeaderName;

        @NotBlank
        private String authToken;
    }
}

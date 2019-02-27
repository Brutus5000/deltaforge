package net.brutus5000.deltaforge.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.brutus5000.deltaforge.client.io.DownloadService;
import net.brutus5000.deltaforge.client.io.SimpleDownloadService;
import net.brutus5000.deltaforge.patching.io.IoService;
import net.brutus5000.deltaforge.patching.io.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DeltaforgeClientProperties.class)
public class DeltaforgeClientAutoConfiguration {
    @Autowired
    private DeltaforgeClientProperties deltaforgeClientProperties;

    @Bean
    @ConditionalOnMissingBean
    public DeltaforgeClient deltaforgeClient(ObjectMapper objectMapper, DownloadService downloadService, RepositoryService repositoryService) {
        return new DeltaforgeClient(deltaforgeClientProperties, objectMapper, downloadService, repositoryService);
    }

    @Bean
    @ConditionalOnMissingBean
    public DownloadService downloadService() {
        return new SimpleDownloadService();
    }

    @Bean
    @ConditionalOnMissingBean
    public IoService ioService() {
        return new IoService();
    }

    @Bean
    @ConditionalOnMissingBean
    public ValidationService validationService(IoService ioService) {
        return new ValidationService(ioService);
    }
}

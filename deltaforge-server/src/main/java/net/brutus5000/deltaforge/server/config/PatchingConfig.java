package net.brutus5000.deltaforge.server.config;

import net.brutus5000.deltaforge.patching.io.Bsdiff4Service;
import net.brutus5000.deltaforge.patching.io.IoService;
import net.brutus5000.deltaforge.patching.io.ValidationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PatchingConfig {
    @Bean
    public Bsdiff4Service bsdiff4Service() {
        return new Bsdiff4Service();
    }

    @Bean
    public IoService ioService() {
        return new IoService();
    }

    @Bean
    public ValidationService validationService(IoService ioService) {
        return new ValidationService(ioService);
    }
}

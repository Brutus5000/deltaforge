package net.brutus5000.deltaforge.server;

import net.brutus5000.deltaforge.server.config.DeltaforgeServerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({DeltaforgeServerProperties.class})
public class DeltaforgeServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeltaforgeServerApplication.class, args);
    }

}

package net.brutus5000.deltaforge.server.config.swagger;

import com.google.common.base.Predicate;
import io.swagger.annotations.Api;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;

@Configuration
@EnableSwagger2
@Api(value = "Data API")
public class SwaggerConfig {

    @Bean
    public Docket newsApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select().paths(paths())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Deltaforge API")
                .description("To get more information about the entities, choose \"data\" in the top right combobox.")
                .license("MIT")
                .licenseUrl("https://github.com/Brutus5000/deltaforge//blob/develop/LICENSE")
                .build();
    }

    private Predicate<String> paths() {
        return or(
                regex("/data/.*")
        );
    }
}

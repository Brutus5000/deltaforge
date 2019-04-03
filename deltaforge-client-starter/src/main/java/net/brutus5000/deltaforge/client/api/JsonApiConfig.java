package net.brutus5000.deltaforge.client.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.annotations.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Class.forName;

@Configuration
public class JsonApiConfig {

    @Bean
    public ResourceConverter resourceConverter(ObjectMapper objectMapper) {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return new ResourceConverter(objectMapper, findJsonApiTypes("net.brutus5000.deltaforge.api.dto"));
    }

    private Class<?>[] findJsonApiTypes(String... scanPackages) {
        List<Class<?>> classes = new ArrayList<>();
        for (String packageName : scanPackages) {
            ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
            provider.addIncludeFilter(new AnnotationTypeFilter(Type.class));
            provider.findCandidateComponents(packageName).stream()
                    .map(beanDefinition -> {
                        try {
                            return (Class) forName(beanDefinition.getBeanClassName());
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .forEach(classes::add);
        }
        return classes.toArray(new Class<?>[classes.size()]);
    }
}

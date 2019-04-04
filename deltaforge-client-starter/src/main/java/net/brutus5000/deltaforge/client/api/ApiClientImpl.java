package net.brutus5000.deltaforge.client.api;

import lombok.extern.slf4j.Slf4j;
import net.brutus5000.deltaforge.api.dto.RepositoryDto;
import net.brutus5000.deltaforge.client.DeltaforgeClientProperties;
import net.brutus5000.deltaforge.client.model.Repository;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class ApiClientImpl implements ApiClient {
    private final static String JSONAPI_PREFIX = "/data";
    private final static String REPOSITORY_QUERY = "/repository?include=channels,tags,patches&filter=name==\"%s\"";

    private final RestTemplate restTemplate;
    private final DeltaforgeClientProperties properties;
    private final ApiDtoMapper apiDtoMapper;

    public ApiClientImpl(RestTemplateBuilder restTemplateBuilder,
                         JsonApiMessageConverter jsonApiMessageConverter,
                         JsonApiErrorHandler jsonApiErrorHandler,
                         DeltaforgeClientProperties properties, ApiDtoMapper apiDtoMapper) {
        this.properties = properties;
        this.apiDtoMapper = apiDtoMapper;

        this.restTemplate = restTemplateBuilder
                .additionalMessageConverters(jsonApiMessageConverter)
                .errorHandler(jsonApiErrorHandler)
                .rootUri(properties.getServerApiUrl())
                .build();
    }

    @Override
    public Optional<Repository> getRepository(String repositoryName) throws IOException {
        log.debug("Get repository from server by name: {}", repositoryName);

        @SuppressWarnings("unchecked") List<RepositoryDto> resultList =
                restTemplate.getForObject(String.format(JSONAPI_PREFIX + REPOSITORY_QUERY, repositoryName), List.class);

        if (resultList == null || resultList.size() == 0) {
            return Optional.empty();
        } else if (resultList.size() > 1) {
            throw new IOException("Found more than one repository with name: " + repositoryName);
        } else {
            Repository repository = apiDtoMapper.map(resultList.get(0));
            return Optional.of(repository);
        }
    }
}

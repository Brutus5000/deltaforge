package net.brutus5000.deltaforge.client.api;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ReflectionUtils;
import com.github.jasminb.jsonapi.ResourceConverter;
import lombok.SneakyThrows;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Collection;

/**
 * A component wrapping the jasminb jsonapi-converter for use in Spring {@see RestTemplate}
 */
@Component
public class JsonApiMessageConverter extends AbstractHttpMessageConverter<Object> {
    private final ResourceConverter resourceConverter;

    public JsonApiMessageConverter(ResourceConverter resourceConverter) {
        super(MediaType.parseMediaType("application/vnd.api+json"));
        this.resourceConverter = resourceConverter;
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return Collection.class.isAssignableFrom(clazz)
                || ReflectionUtils.getTypeName(clazz) != null;
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("unchecked")
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) {
        try (InputStream inputStream = inputMessage.getBody()) {
            JSONAPIDocument<?> document;
            if (Iterable.class.isAssignableFrom(clazz)) {
                document = resourceConverter.readDocumentCollection(inputStream, Object.class);
            } else {
                document = resourceConverter.readDocument(inputMessage.getBody(), Object.class);
            }

            return document.get();
        }
    }

    @Override
    @SneakyThrows
    protected void writeInternal(Object o, HttpOutputMessage outputMessage) {
        if (o instanceof Iterable) {
            outputMessage.getBody().write(resourceConverter.writeDocumentCollection(new JSONAPIDocument<Iterable<?>>((Iterable<?>) o)));
        } else {
            outputMessage.getBody().write(resourceConverter.writeDocument(new JSONAPIDocument<>(o)));
        }
    }
}

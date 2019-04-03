package net.brutus5000.deltaforge.server.model.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.brutus5000.deltaforge.patching.meta.validate.ValidateMetadata;

import javax.persistence.AttributeConverter;
import java.io.IOException;

public class ValidateMetadataConverter implements AttributeConverter<ValidateMetadata, String> {

    private final ObjectMapper objectMapper;

    public ValidateMetadataConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String convertToDatabaseColumn(ValidateMetadata attribute) {
        if (attribute == null)
            return null;

        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ValidateMetadata convertToEntityAttribute(String dbData) {
        if (dbData == null)
            return null;

        try {
            return objectMapper.readValue(dbData, ValidateMetadata.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

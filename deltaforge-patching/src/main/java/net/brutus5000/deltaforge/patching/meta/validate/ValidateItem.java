package net.brutus5000.deltaforge.patching.meta.validate;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import net.brutus5000.deltaforge.patching.meta.IoType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ValidateFileItem.class, name = IoType.FILE),
        @JsonSubTypes.Type(value = ValidateDirectoryItem.class, name = IoType.DIRECTORY),
        @JsonSubTypes.Type(value = ValidateCompressedItem.class, name = IoType.COMPRESSED_FILE)
})
public interface ValidateItem {
    String getName();
}

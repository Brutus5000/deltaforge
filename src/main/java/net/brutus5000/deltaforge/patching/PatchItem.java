package net.brutus5000.deltaforge.patching;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PatchFileItem.class, name = "FILE"),
        @JsonSubTypes.Type(value = PatchDirectoryItem.class, name = "DIRECTORY"),
        @JsonSubTypes.Type(value = PatchCompressedItem.class, name = "COMPRESSED_FILE")
})
public interface PatchItem {
    String getName();

    PatchAction getAction();
}

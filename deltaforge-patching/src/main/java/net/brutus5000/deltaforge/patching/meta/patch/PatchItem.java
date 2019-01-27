package net.brutus5000.deltaforge.patching.meta.patch;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import net.brutus5000.deltaforge.patching.meta.IoType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PatchFileItem.class, name = IoType.FILE),
        @JsonSubTypes.Type(value = PatchDirectoryItem.class, name = IoType.DIRECTORY),
        @JsonSubTypes.Type(value = PatchCompressedItem.class, name = IoType.COMPRESSED_FILE)
})
public interface PatchItem {
    String getName();

    PatchAction getAction();

    boolean requiresInitialBaseline();
}

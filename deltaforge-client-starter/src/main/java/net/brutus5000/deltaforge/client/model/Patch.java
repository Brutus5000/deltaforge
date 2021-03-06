package net.brutus5000.deltaforge.client.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.brutus5000.deltaforge.patching.meta.patch.PatchMetadata;

import java.text.MessageFormat;
import java.time.OffsetDateTime;

@Data
@EqualsAndHashCode(of = {"id", "from", "to"})
@ToString(of = {"id", "from", "to"})
public class Patch {
    public static final String DELTAFORGE_PATCH_PATTERN = "{0}__to__{1}";

    private String id;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    @JsonBackReference("patches")
    private Repository repository;
    private Tag from;
    private Tag to;
    private String filePath;
    private Long fileSize;
    private PatchMetadata metadata;

    @JsonIgnore
    public String getName() {
        return MessageFormat.format(DELTAFORGE_PATCH_PATTERN,
                from.getName(),
                to.getName()
        );
    }
}

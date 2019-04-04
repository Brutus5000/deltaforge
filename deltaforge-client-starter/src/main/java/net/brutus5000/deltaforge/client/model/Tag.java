package net.brutus5000.deltaforge.client.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.brutus5000.deltaforge.patching.meta.validate.ValidateMetadata;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * A tag references a defined state of a repository.
 */
@Data
@EqualsAndHashCode(of = {"id", "name"})
@ToString(of = {"id", "name"})
public class Tag {
    private String id;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    @JsonBackReference("tags")
    private Repository repository;
    private String name;
    private Set<TagAssignment> assignments = new HashSet<>();
    private TagType type;

    private ValidateMetadata validateMetadata;
}

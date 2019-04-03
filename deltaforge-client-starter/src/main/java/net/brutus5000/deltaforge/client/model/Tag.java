package net.brutus5000.deltaforge.client.model;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import net.brutus5000.deltaforge.patching.meta.validate.ValidateMetadata;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * A tag references a defined state of a repository.
 */
@Data
@FieldNameConstants
@ToString(exclude = {"repository", "assignments"})
public class Tag {
    private String id;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Repository repository;
    private String name;
    private String gitTagName;
    private String gitCommitId;
    private Set<TagAssignment> assignments = new HashSet<>();
    private TagType type;

    private ValidateMetadata validateMetadata;
}

package net.brutus5000.deltaforge.client.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import net.brutus5000.deltaforge.client.api.RepositoryDto;
import net.brutus5000.deltaforge.client.api.TagAssignmentDto;
import net.brutus5000.deltaforge.patching.meta.validate.ValidateMetadata;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A tag references a defined state of a repository.
 */
@Data
@FieldNameConstants
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"repository", "assignments"})
public class Tag {
    private UUID id;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private RepositoryDto repository;
    private String name;
    private String gitTagName;
    private String gitCommitId;
    private Set<TagAssignmentDto> assignments = new HashSet<>();
    private TagType type;

    private ValidateMetadata validateMetadata;

    @JsonIgnore
    public UUID getRepositoryId() {
        return repository == null ? null : repository.getId();
    }
}

package net.brutus5000.deltaforge.api.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import net.brutus5000.deltaforge.patching.meta.validate.ValidateMetadata;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import static net.brutus5000.deltaforge.api.dto.TagDto.TYPE_NAME;

/**
 * A tag references a defined state of a repository.
 */
@Data
@FieldNameConstants
@EqualsAndHashCode(of = {"id", "name"})
@ToString(exclude = {"repository", "assignments"})
@Type(TYPE_NAME)
public class TagDto {
    public static final String TYPE_NAME = "tag";

    @Id
    private String id;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    @Relationship("repository")
    @JsonBackReference("tagParent")
    private RepositoryDto repository;

    private String name;

    @Relationship("assignments")
    private Set<TagAssignmentDto> assignments = new HashSet<>();

    private TagTypeDto type;

    private ValidateMetadata validateMetadata;

}

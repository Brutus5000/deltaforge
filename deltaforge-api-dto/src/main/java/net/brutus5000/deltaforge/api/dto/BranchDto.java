package net.brutus5000.deltaforge.api.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;

import static net.brutus5000.deltaforge.api.dto.BranchDto.TYPE_NAME;

/**
 * A branch tracks the development of a repository in a certain direction.
 * <p>
 * Branches are required to manage multiple parallel initialBaseline versions. However, it does not limit the clients ability
 * to apply patches across branch.
 * <p>
 * Multiple parallel initialBaseline versions are required when the underlying repository is not just moving forward in one
 * direction. Example: After release of version 1.0 and it's successor 2.0, there is a bugfix version 1.1 that should
 * not be built upon 1.0 (again) instead of 2.0.
 */
@Data
@EqualsAndHashCode(of = {"id", "name"})
@Type(TYPE_NAME)
public class BranchDto {
    public static final String TYPE_NAME = "branch";

    @Id
    private String id;

    private String name;

    @Relationship("repository")
    @JsonBackReference("branchParent")
    private RepositoryDto repository;

    @Relationship("currentTag")
    private TagDto currentTag;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

}

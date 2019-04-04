package net.brutus5000.deltaforge.api.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.brutus5000.deltaforge.patching.meta.patch.PatchMetadata;

import java.time.OffsetDateTime;

import static net.brutus5000.deltaforge.api.dto.PatchDto.TYPE_NAME;

@Data
@EqualsAndHashCode(of = {"id", "from", "to"})
@ToString(of = {"id", "from", "to"})
@Type(TYPE_NAME)
public class PatchDto {
    public static final String TYPE_NAME = "patch";

    @Id
    private String id;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    @Relationship("repository")
    @JsonBackReference("patchParent")
    private RepositoryDto repository;

    @Relationship("from")
    private TagDto from;

    @Relationship("to")
    private TagDto to;

    private String filePath;

    private Long fileSize;

    private PatchMetadata metadata;
}

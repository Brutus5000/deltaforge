package net.brutus5000.deltaforge.api.dto;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import static net.brutus5000.deltaforge.api.dto.RepositoryDto.TYPE_NAME;

/**
 * A repository represents a set (technically: a folder) of binary files under binary version control.
 */
@Data
@EqualsAndHashCode(of = {"id", "name"})
@ToString(of = {"id", "name"})
@Type(TYPE_NAME)
public class RepositoryDto {
    public static final String TYPE_NAME = "repository";

    @Id
    private String id;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    private String name;

    private String folderPath;

    @Relationship("channels")
    @JsonManagedReference("channelParent")
    private Set<ChannelDto> channels = new HashSet<>();

    @Relationship("tags")
    @JsonManagedReference("tagParent")
    private Set<TagDto> tags = new HashSet<>();

    @Relationship("patches")
    @JsonManagedReference("patchParent")
    private Set<PatchDto> patches = new HashSet<>();

    private String patchGraph;
}

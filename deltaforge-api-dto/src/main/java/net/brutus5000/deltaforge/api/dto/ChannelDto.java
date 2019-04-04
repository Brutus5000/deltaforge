package net.brutus5000.deltaforge.api.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.OffsetDateTime;

import static net.brutus5000.deltaforge.api.dto.ChannelDto.TYPE_NAME;

/**
 * A channel represent different versioning concepts (e.g. unstable / testing / stable).
 * It always points to the latest tag available.
 */
@Data
@EqualsAndHashCode(of = {"id", "name"})
@ToString(of = {"id", "name", "currentTag"})
@Type(TYPE_NAME)
public class ChannelDto {
    public static final String TYPE_NAME = "channel";

    @Id
    private String id;

    private String name;

    @Relationship("repository")
    @JsonBackReference("channelParent")
    private RepositoryDto repository;

    @Relationship("currentTag")
    private TagDto currentTag;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

}

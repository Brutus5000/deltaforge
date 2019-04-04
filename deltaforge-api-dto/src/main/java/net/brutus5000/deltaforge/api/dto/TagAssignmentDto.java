package net.brutus5000.deltaforge.api.dto;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.OffsetDateTime;

import static net.brutus5000.deltaforge.api.dto.TagAssignmentDto.TYPE_NAME;

@Data
@EqualsAndHashCode(of = {"id", "channel", "tag"})
@ToString(of = {"id"})
@Type(TYPE_NAME)
public class TagAssignmentDto {
    public static final String TYPE_NAME = "tagAssignment";

    @Id
    private String id;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private ChannelDto channel;
    private TagDto tag;
}

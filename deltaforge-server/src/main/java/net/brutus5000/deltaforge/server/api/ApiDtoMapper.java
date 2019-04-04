package net.brutus5000.deltaforge.server.api;

import net.brutus5000.deltaforge.api.dto.*;
import net.brutus5000.deltaforge.server.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", uses = CycleAvoidingMappingContext.class)
public interface ApiDtoMapper {
    default String map(UUID uuid) {
        return uuid.toString();
    }

    default UUID map(String uuidString) {
        return UUID.fromString(uuidString);
    }

    @Mapping(target = "patchGraph", ignore = true)
    RepositoryDto map(Repository repository);

    @Mapping(target = "patchGraph", ignore = true)
    Repository map(RepositoryDto repositoryDto);

    List<RepositoryDto> map(List<Repository> repositoryList);

    ChannelDto map(Channel channel);

    Channel map(ChannelDto channelDto);

    PatchDto map(Patch patch);

    Patch map(PatchDto patchDto);

    TagDto map(Tag tag);

    Tag map(TagDto tagDto);

    TagAssignmentDto map(TagAssignment tagAssignment);

    TagAssignment map(TagAssignmentDto tagAssignmentDto);

    TagTypeDto map(TagType tagType);

    TagType map(TagTypeDto tagTypeDto);
}

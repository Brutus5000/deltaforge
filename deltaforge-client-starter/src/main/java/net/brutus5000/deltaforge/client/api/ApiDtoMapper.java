package net.brutus5000.deltaforge.client.api;

import net.brutus5000.deltaforge.api.dto.*;
import net.brutus5000.deltaforge.client.model.*;
import org.mapstruct.Mapper;

/**
 * This interface describes a mapper for api dto objects to internal objects.
 * The implementation is generated before compile time by Mapstruct.
 * It is available as a regular Spring bean.
 */
@Mapper(componentModel = "spring", uses = CycleAvoidingMappingContext.class)
public interface ApiDtoMapper {
    Repository map(RepositoryDto repositoryDto);

    ChannelDto map(ChannelDto channelDto);

    Patch map(PatchDto patchDto);

    Tag map(TagDto tagDto);

    TagAssignment map(TagAssignmentDto tagAssignmentDto);

    TagType map(TagTypeDto tagTypeDto);
}

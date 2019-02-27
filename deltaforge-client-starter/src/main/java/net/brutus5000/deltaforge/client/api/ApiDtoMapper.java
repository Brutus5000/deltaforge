package net.brutus5000.deltaforge.client.api;

import net.brutus5000.deltaforge.client.model.*;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApiDtoMapper {

    Repository map(RepositoryDto repositoryDto);

    Branch map(BranchDto branchDto);

    Patch map(PatchDto patchDto);

    Tag map(TagDto tagDto);

    TagAssignment map(TagAssignmentDto tagAssignmentDto);

    TagType map(TagTypeDto tagTypeDto);
}

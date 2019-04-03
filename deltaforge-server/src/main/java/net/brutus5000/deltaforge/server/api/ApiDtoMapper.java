package net.brutus5000.deltaforge.server.api;

import net.brutus5000.deltaforge.api.dto.*;
import net.brutus5000.deltaforge.server.model.*;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = CycleAvoidingMappingContext.class)
public interface ApiDtoMapper {

    RepositoryDto map(Repository repository);

    Repository map(RepositoryDto repositoryDto);

    List<RepositoryDto> map(List<Repository> repositoryList);

    BranchDto map(Branch branch);

    Branch map(BranchDto branchDto);

    PatchDto map(Patch patch);

    Patch map(PatchDto patchDto);

    TagDto map(Tag tag);

    Tag map(TagDto tagDto);

    TagAssignmentDto map(TagAssignment tagAssignment);

    TagAssignment map(TagAssignmentDto tagAssignmentDto);

    TagTypeDto map(TagType tagType);

    TagType map(TagTypeDto tagTypeDto);
}

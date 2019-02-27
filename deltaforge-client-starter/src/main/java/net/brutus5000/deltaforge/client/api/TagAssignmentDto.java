package net.brutus5000.deltaforge.client.api;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class TagAssignmentDto {
    private UUID id;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private BranchDto branch;
    private TagDto tag;
}

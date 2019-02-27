package net.brutus5000.deltaforge.client.model;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class TagAssignment {
    private UUID id;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Branch branch;
    private Tag tag;
}

package net.brutus5000.deltaforge.client.api;

import lombok.Data;
import net.brutus5000.deltaforge.client.model.Repository;
import net.brutus5000.deltaforge.patching.meta.patch.PatchMetadata;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class PatchDto {
    private UUID id;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Repository repository;
    private TagDto from;
    private TagDto to;
    private String filePath;
    private Long fileSize;
    private PatchMetadata metadata;
}

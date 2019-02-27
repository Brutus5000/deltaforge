package net.brutus5000.deltaforge.client.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A repository represents a set (technically: a folder) of binary files under binary version control.
 */
@Data
@Slf4j
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"branches", "tags", "patches"})
public class RepositoryDto {
    private UUID id;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String name;
    private String folderPath;
    private String gitUrl;
    private Set<BranchDto> branches = new HashSet<>();
    private Set<TagDto> tags = new HashSet<>();
    private Set<PatchDto> patches = new HashSet<>();
    private String graph;
}

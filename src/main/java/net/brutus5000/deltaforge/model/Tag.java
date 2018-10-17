package net.brutus5000.deltaforge.model;

import lombok.Data;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * A tag references a defined state of a repository.
 */
@Entity
@Data
public class Tag implements UniqueEntity {
    @Id
    @GeneratedValue
    private UUID id;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String name;
    private String gitTagName;
    private String gitCommitId;
    @Enumerated(EnumType.STRING)
    private TagType type;
}

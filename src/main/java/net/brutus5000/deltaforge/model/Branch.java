package net.brutus5000.deltaforge.model;

import lombok.Data;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * A branch tracks the development of a repository in a certain direction.
 * <p>
 * Branches are required to manage multiple parallel baseline versions. However, it does not limit the clients ability
 * to apply patches across branches.
 * <p>
 * Multiple parallel baseline versions are required when the underlying repository is not just moving forward in one
 * direction. Example: After release of version 1.0 and it's successor 2.0, there is a bugfix version 1.1 that should
 * not be built upon 1.0 (again) instead of 2.0.
 */
@Entity
@Data
public class Branch implements UniqueEntity {
    @Id
    @GeneratedValue
    private UUID id;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String name;
    @ManyToOne
    @JoinColumn
    private Repository repository;
    private Tag initialBaseline;
    private Tag currentBaseline;
    private Tag currentTag;
}

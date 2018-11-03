package net.brutus5000.deltaforge.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * A branch tracks the development of a repository in a certain direction.
 * <p>
 * Branches are required to manage multiple parallel initialBaseline versions. However, it does not limit the clients ability
 * to apply patches across branches.
 * <p>
 * Multiple parallel initialBaseline versions are required when the underlying repository is not just moving forward in one
 * direction. Example: After release of version 1.0 and it's successor 2.0, there is a bugfix version 1.1 that should
 * not be built upon 1.0 (again) instead of 2.0.
 */
@Entity
@Data
@EqualsAndHashCode(of = "id")
public class Branch implements UniqueEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @CreationTimestamp
    private OffsetDateTime createdAt;
    @UpdateTimestamp
    private OffsetDateTime updatedAt;
    @Column(unique = true, nullable = false)
    private String name;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Repository repository;
    @OneToOne
    @JoinColumn(nullable = false)
    private Tag initialBaseline;
    @OneToOne
    @JoinColumn(nullable = false)
    private Tag currentBaseline;
    @OneToOne
    @JoinColumn(nullable = false)
    private Tag currentTag;
    private String graph;
}

package net.brutus5000.deltaforge.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * A tag references a defined state of a repository.
 */
@Entity
@Data
@FieldNameConstants
public class Tag implements UniqueEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @CreationTimestamp
    private OffsetDateTime createdAt;
    @UpdateTimestamp
    private OffsetDateTime updatedAt;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Repository repository;
    @Column(unique = true, nullable = false)
    private String name;
    private String gitTagName;
    private String gitCommitId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TagType type;

    @Transient
    @JsonIgnore
    public UUID getRepositoryId() {
        return repository == null ? null : repository.getId();
    }
}

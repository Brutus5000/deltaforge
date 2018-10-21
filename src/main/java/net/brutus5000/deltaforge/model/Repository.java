package net.brutus5000.deltaforge.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * A repository represents a set (technically: a folder) of binary files under binary version control.
 */
@Entity
@Data
public class Repository implements UniqueEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @CreationTimestamp
    private OffsetDateTime createdAt;
    @UpdateTimestamp
    private OffsetDateTime updatedAt;
    @Column(unique = true, nullable = false)
    private String name;
    @Column(unique = true)
    private String folderPath;
    @Column(unique = true)
    private String gitUrl;
    @OneToMany(mappedBy = "repository")
    private Set<Branch> branches;
    @OneToMany(mappedBy = "repository")
    private Set<Tag> tags;
    private String patchGraph;
}

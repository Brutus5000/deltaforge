package net.brutus5000.deltaforge.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
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
    @GeneratedValue
    private UUID id;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String name;
    private String folderPath;
    private String gitUrl;
    @OneToMany(mappedBy = "repository")
    private Set<Branch> branches;
    private String patchGraph;
}

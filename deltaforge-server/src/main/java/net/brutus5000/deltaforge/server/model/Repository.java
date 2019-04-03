package net.brutus5000.deltaforge.server.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yahoo.elide.annotation.Include;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.brutus5000.deltaforge.api.dto.RepositoryDto;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A repository represents a set (technically: a folder) of binary files under binary version control.
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = "name")})
@Data
@Slf4j
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"branches", "tags", "patches", "patchGraph"})
@Include(rootLevel = true, type = RepositoryDto.TYPE_NAME)
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
    @OneToOne
    @JoinColumn
    private Tag initialBaseline;
    @OneToMany(mappedBy = "repository")
    @JsonManagedReference
    private Set<Branch> branches = new HashSet<>();
    @OneToMany(mappedBy = "repository")
    private Set<Tag> tags = new HashSet<>();
    @OneToMany(mappedBy = "repository")
    @JsonManagedReference
    private Set<Patch> patches = new HashSet<>();

    @Transient
    @JsonProperty
    private PatchGraph patchGraph;

    @PostLoad
    @PostPersist
    public void initializePathGraph() {
        log.debug("Building graph for repository: {}", this);

        patchGraph = new PatchGraph();

        tags.forEach(patchGraph::addVertex);
        patches.forEach(patchGraph::addEdge);
    }
}

package net.brutus5000.deltaforge.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedWeightedPseudograph;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A repository represents a set (technically: a folder) of binary files under binary version control.
 */
@Entity
@Data
@Slf4j
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"branches", "tags", "patches", "patchGraph"})
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
    @OneToOne
    @JoinColumn
    private Tag initialBaseline;
    @OneToMany(mappedBy = "repository")
    private Set<Branch> branches = new HashSet<>();
    @OneToMany(mappedBy = "repository")
    private Set<Tag> tags = new HashSet<>();
    @OneToMany(mappedBy = "repository")
    private Set<Patch> patches = new HashSet<>();

    @Transient
    @JsonIgnore
    private Graph<Tag, Patch> patchGraph;

    @PostLoad
    @PostPersist
    public void initializePathGraph() {
        log.debug("Building graph for repository: {}", this);

        patchGraph = new DirectedWeightedPseudograph<>(Patch.class);

        tags.forEach(patchGraph::addVertex);
        patches.forEach(patch -> patchGraph.addEdge(patch.getFrom(), patch.getTo(), patch));
        patchGraph.edgeSet().forEach(patch -> patchGraph.setEdgeWeight(patch, patch.getFileSize()));
    }
}

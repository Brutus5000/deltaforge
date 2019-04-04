package net.brutus5000.deltaforge.server.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.yahoo.elide.annotation.Include;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.brutus5000.deltaforge.api.dto.ChannelDto;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * A channel represent different versioning concepts (e.g. unstable / testing / stable).
 * It always points to the latest tag available.
 */
@Entity
@Data
@EqualsAndHashCode(of = {"id", "name"})
@ToString(of = {"id", "name", "currentTag"})
@Include(type = ChannelDto.TYPE_NAME)
public class Channel implements UniqueEntity {
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
    @JsonBackReference
    private Repository repository;
    @OneToOne
    @JoinColumn(nullable = false)
    private Tag currentBaseline;
    @OneToOne
    @JoinColumn(nullable = false)
    private Tag currentTag;
    private String graph;
}

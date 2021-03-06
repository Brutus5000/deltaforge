package net.brutus5000.deltaforge.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.SharePermission;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.brutus5000.deltaforge.api.dto.TagDto;
import net.brutus5000.deltaforge.patching.meta.validate.ValidateMetadata;
import net.brutus5000.deltaforge.server.model.converter.ValidateMetadataConverter;
import net.brutus5000.deltaforge.server.model.listener.TagListener;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A tag references a defined state of a repository (like a "version").
 */
@Entity
@Data
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"repository", "assignments"})
@Include(type = TagDto.TYPE_NAME)
@SharePermission
@EntityListeners(TagListener.class)
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

    @OneToMany(mappedBy = "tag")
    private Set<TagAssignment> assignments = new HashSet<>();

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TagType type;

    @Column(length = 1000000) // TODO: Remove this as soon as we move to Postgres
    @Convert(converter = ValidateMetadataConverter.class)
    private ValidateMetadata validateMetadata;

    @Transient
    @JsonIgnore
    public UUID getRepositoryId() {
        return repository == null ? null : repository.getId();
    }
}

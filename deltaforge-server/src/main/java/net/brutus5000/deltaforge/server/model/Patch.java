package net.brutus5000.deltaforge.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yahoo.elide.annotation.Include;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import net.brutus5000.deltaforge.api.dto.PatchDto;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.text.MessageFormat;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
@FieldNameConstants
@Include(type = PatchDto.TYPE_NAME)
public class Patch implements UniqueEntity {
    public static final String DELTAFORGE_PATCH_PATTERN = "{0}__to__{1}";

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
    @ManyToOne
    @JoinColumn(nullable = false)
    private Tag from;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Tag to;
    @JoinColumn(nullable = false)
    private String filePath;
    private Long fileSize;

    @Transient
    @JsonIgnore
    public String getName() {
        return MessageFormat.format(DELTAFORGE_PATCH_PATTERN,
                from.getName(),
                to.getName()
        );
    }
}

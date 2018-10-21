package net.brutus5000.deltaforge.model;

import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
@FieldNameConstants
public class Patch implements UniqueEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @CreationTimestamp
    private OffsetDateTime createdAt;
    @UpdateTimestamp
    private OffsetDateTime updatedAt;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Tag from;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Tag to;
    @JoinColumn(nullable = false)
    private String patchFilePath;

    @Transient
    public Path getFilePath() {
        return Paths.get(patchFilePath);
    }
}

package net.brutus5000.deltaforge.model;

import lombok.Data;

import javax.persistence.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
public class Patch implements UniqueEntity {
    @Id
    @GeneratedValue
    private UUID id;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    @ManyToOne
    @JoinColumn
    private Tag from;
    @ManyToOne
    @JoinColumn
    private Tag to;
    private String patchFilePath;

    @Transient
    public Path getFilePath() {
        return Paths.get(patchFilePath);
    }
}

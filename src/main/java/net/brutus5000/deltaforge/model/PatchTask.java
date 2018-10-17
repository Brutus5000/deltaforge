package net.brutus5000.deltaforge.model;

import lombok.Data;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * A work log record of creating a patch.
 * As opposed to the Task, multiple patch tasks can exist for the same from->to path
 * since not every task is actually successful.
 */
@Entity
@Data
public class PatchTask implements UniqueEntity {
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
    @Enumerated(EnumType.STRING)
    private TaskStatus status;
    private String errorMessage;
}

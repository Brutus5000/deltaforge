package net.brutus5000.deltaforge.model;

import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
@FieldNameConstants
public class PatchTask implements UniqueEntity {
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
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;
    private String errorMessage;
}

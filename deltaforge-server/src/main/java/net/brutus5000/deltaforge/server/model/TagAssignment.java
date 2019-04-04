package net.brutus5000.deltaforge.server.model;

import com.yahoo.elide.annotation.Include;
import lombok.Data;
import net.brutus5000.deltaforge.api.dto.TagAssignmentDto;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
@Include(type = TagAssignmentDto.TYPE_NAME)
public class TagAssignment implements UniqueEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @CreationTimestamp
    private OffsetDateTime createdAt;
    @UpdateTimestamp
    private OffsetDateTime updatedAt;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Channel channel;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Tag tag;
}

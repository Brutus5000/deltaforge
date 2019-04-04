package net.brutus5000.deltaforge.client.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.OffsetDateTime;

/**
 * A channel represent different versioning concepts (e.g. unstable / testing / stable).
 * It always points to the latest tag available.
 */
@Data
@EqualsAndHashCode(of = {"id", "name"})
@ToString(of = {"id", "name", "currentTag"})
public class Channel {
    private String id;
    private String name;
    @JsonBackReference("channels")
    private Repository repository;
    private Tag currentTag;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

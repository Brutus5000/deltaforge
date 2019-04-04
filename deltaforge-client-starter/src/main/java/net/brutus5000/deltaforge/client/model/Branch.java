package net.brutus5000.deltaforge.client.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * A branch tracks the development of a repository in a certain direction.
 * <p>
 * Branches are required to maintain multiple release tracks. Example: After release of version 1.0 and it's successor
 * 2.0, there is a bugfix version 1.1 that should be built upon 1.0 (again) instead of 2.0.
 */
@Data
public class Branch {
    private String id;
    private String name;
    @JsonBackReference("branches")
    private Repository repository;
    private Tag currentTag;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

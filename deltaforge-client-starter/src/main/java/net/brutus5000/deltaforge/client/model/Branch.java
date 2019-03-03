package net.brutus5000.deltaforge.client.model;

import lombok.Data;

import java.time.OffsetDateTime;

/**
 * A branch tracks the development of a repository in a certain direction.
 * <p>
 * Branches are required to manage multiple parallel initialBaseline versions. However, it does not limit the clients ability
 * to apply patches across branches.
 * <p>
 * Multiple parallel initialBaseline versions are required when the underlying repository is not just moving forward in one
 * direction. Example: After release of version 1.0 and it's successor 2.0, there is a bugfix version 1.1 that should
 * not be built upon 1.0 (again) instead of 2.0.
 */
@Data
public class Branch {
    private String name;
    private Repository repository;
    private Tag currentTag;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

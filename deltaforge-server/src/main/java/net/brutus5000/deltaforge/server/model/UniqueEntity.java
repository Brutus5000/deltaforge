package net.brutus5000.deltaforge.server.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface UniqueEntity {

    UUID getId();

    OffsetDateTime getCreatedAt();

    OffsetDateTime getUpdatedAt();

    UniqueEntity setUpdatedAt(OffsetDateTime updateODT);
}

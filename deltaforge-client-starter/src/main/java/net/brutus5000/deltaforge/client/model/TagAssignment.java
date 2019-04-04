package net.brutus5000.deltaforge.client.model;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class TagAssignment {
    private String id;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Channel channel;
    private Tag tag;
}

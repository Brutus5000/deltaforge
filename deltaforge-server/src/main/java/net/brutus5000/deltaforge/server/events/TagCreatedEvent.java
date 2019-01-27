package net.brutus5000.deltaforge.server.events;

import lombok.Value;
import net.brutus5000.deltaforge.server.model.Tag;

@Value
public class TagCreatedEvent {
    private final Tag tag;
}

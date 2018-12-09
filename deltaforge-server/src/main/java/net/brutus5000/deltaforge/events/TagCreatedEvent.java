package net.brutus5000.deltaforge.events;

import lombok.Value;
import net.brutus5000.deltaforge.model.Tag;

@Value
public class TagCreatedEvent {
    private final Tag tag;
}

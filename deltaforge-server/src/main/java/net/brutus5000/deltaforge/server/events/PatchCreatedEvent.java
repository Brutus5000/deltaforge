package net.brutus5000.deltaforge.server.events;

import lombok.Value;
import net.brutus5000.deltaforge.server.model.Patch;

@Value
public class PatchCreatedEvent {
    private final Patch patch;
    /**
     * If true, a check for upgrade to initialBaseline check has to be performed
     */
    private final boolean baselineCheck;
}

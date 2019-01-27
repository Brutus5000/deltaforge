package net.brutus5000.deltaforge.patching.meta.patch;

import lombok.Data;

@Data
public class PatchFileItem implements PatchItem {
    private String name;
    private PatchAction action;
    /**
     * Checksum of the original file
     */
    private String baseCrc;
    /**
     * Checksum of the target file
     */
    private String targetCrc;

    @Override
    public boolean requiresInitialBaseline() {
        return action == PatchAction.BSDIFF_FROM_INITIAL_BASELINE;
    }
}

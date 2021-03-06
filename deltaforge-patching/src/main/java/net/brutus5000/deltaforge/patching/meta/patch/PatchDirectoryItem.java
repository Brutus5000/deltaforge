package net.brutus5000.deltaforge.patching.meta.patch;

import lombok.Data;

import java.util.Set;

@Data
public class PatchDirectoryItem implements PatchItem {
    private String name;
    private PatchAction action;
    private Set<PatchItem> items;

    @Override
    public boolean requiresInitialBaseline() {
        for (PatchItem subItem : items) {
            if (subItem.requiresInitialBaseline()) {
                return true;
            }
        }

        return false;
    }
}

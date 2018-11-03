package net.brutus5000.deltaforge.patching;

import lombok.Data;

import java.util.Set;

@Data
public class PatchDirectoryItem implements PatchItem {
    private String name;
    private PatchAction action;
    private Set<PatchItem> items;
}

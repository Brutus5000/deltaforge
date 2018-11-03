package net.brutus5000.deltaforge.patching;

import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class PatchMetadata {
    private String repository;
    private int protocol;
    private String fromTag;
    private String toTag;
    private Set<PatchItem> items;
    private Map<String, String> fileRenaming;
}

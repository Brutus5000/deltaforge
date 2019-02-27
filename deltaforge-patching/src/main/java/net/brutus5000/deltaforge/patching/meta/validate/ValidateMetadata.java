package net.brutus5000.deltaforge.patching.meta.validate;

import lombok.Data;

import java.util.Set;

@Data
public class ValidateMetadata {
    private String repository;
    private String tag;
    private Set<ValidateItem> items;
}

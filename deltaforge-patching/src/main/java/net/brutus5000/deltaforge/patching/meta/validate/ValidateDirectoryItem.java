package net.brutus5000.deltaforge.patching.meta.validate;

import lombok.Data;

import java.util.Set;

@Data
public class ValidateDirectoryItem implements ValidateItem {
    private String name;

    private Set<ValidateItem> items;
}

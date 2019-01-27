package net.brutus5000.deltaforge.patching.meta.validate;

import lombok.Data;

import java.util.Set;

@Data
public class ValidateCompressedItem implements ValidateItem {
    private String name;
    private String algorithm;
    private Set<ValidateItem> items;
}

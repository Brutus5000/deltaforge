package net.brutus5000.deltaforge.patching.meta.validate;

import lombok.Data;

@Data
public class ValidateFileItem implements ValidateItem {
    private String name;
    private String crc;
}

package net.brutus5000.deltaforge.patching.meta.patch;

import lombok.Data;

import java.nio.file.Path;

@Data
public class PatchRequest extends PatchMetadata {
    private Path sourceFolder;
    private Path initialBaselineFolder;
    private Path targetFolder;
    private Path patchFolder;
}

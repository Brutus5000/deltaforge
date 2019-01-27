package net.brutus5000.deltaforge.patching.meta.patch;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum PatchAction {
    @JsonProperty("unchanged")
    UNCHANGED,
    @JsonProperty("add")
    ADD,
    @JsonProperty("remove")
    REMOVE,
    @JsonProperty("delta")
    DELTA,
    @JsonProperty("bsdiff")
    BSDIFF,
    @JsonProperty("bsdiffFromInitialBaseline")
    BSDIFF_FROM_INITIAL_BASELINE,
    @JsonProperty("compressedFile")
    COMPRESSED_FILE
}

package net.brutus5000.deltaforge.patching;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum IoType {
    @JsonProperty("file")
    FILE,
    @JsonProperty("directory")
    DIRECTORY,
    /**
     * A compressed file archive
     * To be treated as a directory, but needs to be extracted first and compressed again after patching
     */
    @JsonProperty("compressedFile")
    COMPRESSED_FILE
}

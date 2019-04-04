package net.brutus5000.deltaforge.server.model;

import net.brutus5000.deltaforge.server.error.ApiException;
import net.brutus5000.deltaforge.server.error.ErrorCode;

import java.util.Objects;

public enum TagType {
    /**
     * A source tag references a known but unusable state.
     * It is desired to patch from here to the initial initialBaseline, but never in reverse.
     * <p>
     * Implication: In theory source tags don't need to be kept in original binary state after creating the patches
     * to their destinated initialBaseline. It is however strongly recommended to do this anyway if possible (i.e. in case
     * of corrupted repository or other cases of desaster recovery).
     */
    SOURCE,
    /**
     * A baseline tag is a special marker of a stable tag. New baselines are created after a defined amount of
     * intermediate patches or when the overall size of all delta patches from the last baselines exceeds a defined
     * threshold.
     * <p>
     * Implication: Baseline tags need to be kept in original binary state, to be able to create new delta patches to
     * it's intermediate tags and all other baseline tags.
     */
    BASELINE,
    /**
     * An intermediate tag references a stable state that has only a tiny delta to the parent baseline version.
     * Intermediate tags can follow on baseline tags or other intermediate tags.
     * <p>
     * Implication: intermediate tags need to be kept in original binary state until the next baseline tag is created.
     */
    INTERMEDIATE;

    public static TagType parse(String value) {
        for (TagType t : TagType.values()) {
            if (Objects.equals(value, t.name())) {
                return t;
            }
        }

        throw ApiException.of(ErrorCode.TAG_INVALID_TYPE, value);
    }
}

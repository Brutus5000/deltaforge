package net.brutus5000.deltaforge.model;

public enum TagType {
    /**
     * A source tag references a known but unusable state.
     * It is desired to patch from here to the initial baseline, but never in reverse.
     * <p>
     * Implication: In theory source tags don't need to be kept in original binary state after creating the patches
     * to their destinated baseline. It is however strongly recommended to do this anyway if possible (i.e. in case
     * of corrupted repository or other cases of desaster recovery).
     */
    SOURCE,
    /**
     * A baseline tag references a stable state (not to be confused that is a basis to create delta patches from.
     * New baselines are created after a defined amount of delta patches or when the overall size of all delta patches
     * from the last baselines exceeds a defined threshold.
     * <p>
     * Implication: Baseline tags need to be kept in original binary state, to be able to create new delta patches to
     * it's intermediate tags or all other baseline tags.
     */
    BASELINE,
    /**
     * An intermediate tag references a stable state that has only a tiny delta to the parent baseline version.
     * Intermediate tags can follow on baseline or other intermediate tags.
     * <p>
     * Implication: Baseline tags need to be kept in original binary state until the .
     */
    INTERMEDIATE;
}

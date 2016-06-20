package com.abusalimov.mrcalc.runtime;

/**
 * The runtime serves as a factory for certain types of objects, like {@link #createLongRange(long, long) ranges}.
 *
 * @author Eldar Abusalimov
 */
public class Runtime {
    /**
     * Creates a new {@link LongSequence} filled by integers between the specified boundaries.
     *
     * @param startInclusive the start boundary of the range (inclusive)
     * @param endExclusive   the end boundary of the range (exclusive)
     * @return the new {@link LongSequence} instance
     */
    public LongSequence createLongRange(long startInclusive, long endExclusive) {
        return new LongRange(startInclusive, endExclusive);
    }
}

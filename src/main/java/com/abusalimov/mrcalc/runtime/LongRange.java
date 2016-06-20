package com.abusalimov.mrcalc.runtime;

/**
 * A simple and naive implementation of range of longs.
 *
 * @author Eldar Abusalimov
 */
public class LongRange extends LongSequence {
    /**
     * Creates a new range starting from zero.
     *
     * @param endExclusive the end boundary of the range (exclusive), which is also the length of the range
     */
    public LongRange(long endExclusive) {
        this(0, endExclusive);
    }

    /**
     * Creates a new range filled by integers between the specified boundaries.
     *
     * @param startInclusive the start boundary of the range (inclusive)
     * @param endExclusive   the end boundary of the range (exclusive)
     */
    public LongRange(long startInclusive, long endExclusive) {
        super(createArray(startInclusive, endExclusive));
    }

    private static long[] createArray(long startInclusive, long endExclusive) {
        long longLength = Math.max(0, endExclusive - startInclusive);
        if (longLength > Integer.MAX_VALUE) {
            throw new ArithmeticException("Sequence range size overflow");
        }
        int length = (int) longLength;

        long[] ret = new long[length];
        for (int i = 0; i < length; i++) {
            ret[i] = startInclusive + i;
        }
        return ret;
    }
}

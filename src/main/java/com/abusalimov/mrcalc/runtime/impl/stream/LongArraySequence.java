package com.abusalimov.mrcalc.runtime.impl.stream;

import com.abusalimov.mrcalc.runtime.AbstractSequence;
import com.abusalimov.mrcalc.runtime.Sequence;

import java.util.Arrays;
import java.util.Objects;
import java.util.Spliterator;

/**
 * Implementation of specialized long sequence backed by an array of primitive longs.
 *
 * @author Eldar Abusalimov
 */
public class LongArraySequence extends AbstractSequence<Long> implements Sequence.OfLong {
    private final long[] a;

    /**
     * Creates a new view into the specified array of longs.
     *
     * @param array the non-null array of long values
     */
    public LongArraySequence(long[] array) {
        a = Objects.requireNonNull(array);
    }

    @Override
    public int size() {
        return a.length;
    }

    @Override
    public Long get(int index) {
        return a[index];
    }

    @Override
    public Spliterator.OfLong spliterator() {
        return Arrays.spliterator(a);
    }
}

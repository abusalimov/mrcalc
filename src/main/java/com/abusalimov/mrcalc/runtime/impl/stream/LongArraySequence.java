package com.abusalimov.mrcalc.runtime.impl.stream;

import com.abusalimov.mrcalc.runtime.LongSequence;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Spliterator;

/**
 * Implementation of specialized long sequence backed by an array of primitive longs.
 *
 * @author Eldar Abusalimov
 */
public class LongArraySequence extends AbstractList<Long> implements LongSequence {
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
    public Long set(int index, Long element) {
        Long oldValue = a[index];
        a[index] = element;
        return oldValue;
    }

    @Override
    public Spliterator.OfLong spliterator() {
        return Arrays.spliterator(a);
    }
}

package com.abusalimov.mrcalc.runtime;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Spliterator;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

/**
 * The {@link Sequence} implementation specialized to primitive longs.
 *
 * @author Eldar Abusalimov
 */
public class LongSequence extends AbstractList<Long> implements Sequence<Long> {
    private final long[] a;

    /**
     * Creates a new view into the specified array of longs.
     *
     * @param array the non-null array of long values
     */
    public LongSequence(long[] array) {
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

    public LongStream longStream() {
        return StreamSupport.longStream(spliterator(), false);
    }

    public LongStream longParallelStream() {
        return StreamSupport.longStream(spliterator(), true);
    }
}

package com.abusalimov.mrcalc.runtime;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.LongBinaryOperator;
import java.util.function.LongFunction;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongUnaryOperator;

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

    public long reduce(long identity, LongBinaryOperator operator) {
        return Arrays.stream(a).reduce(identity, operator);
    }

    @SuppressWarnings("unchecked")
    public <R> ObjectSequence<R> mapToObject(LongFunction<? extends R> mapper) {
        Object[] array = Arrays.stream(a).mapToObj(mapper).toArray();
        return new ObjectSequence(array);
    }

    public LongSequence mapToLong(LongUnaryOperator mapper) {
        long[] array = Arrays.stream(a).map(mapper).toArray();
        return new LongSequence(array);
    }

    public DoubleSequence mapToDouble(LongToDoubleFunction mapper) {
        double[] array = Arrays.stream(a).mapToDouble(mapper).toArray();
        return new DoubleSequence(array);
    }
}

package com.abusalimov.mrcalc.runtime;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.LongBinaryOperator;
import java.util.function.LongFunction;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongUnaryOperator;
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

    public long reduce(long identity, LongBinaryOperator operator) {
        return longParallelStream().reduce(identity, operator);
    }

    @SuppressWarnings("unchecked")
    public <R> ObjectSequence<R> mapToObject(LongFunction<? extends R> mapper) {
        return new ObjectSequence(longParallelStream().mapToObj(mapper).toArray());
    }

    public LongSequence mapToLong(LongUnaryOperator mapper) {
        return new LongSequence(longParallelStream().map(mapper).toArray());
    }

    public DoubleSequence mapToDouble(LongToDoubleFunction mapper) {
        return new DoubleSequence(longParallelStream().mapToDouble(mapper).toArray());
    }
}

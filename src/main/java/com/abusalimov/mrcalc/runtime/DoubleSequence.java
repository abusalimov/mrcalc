package com.abusalimov.mrcalc.runtime;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleFunction;
import java.util.function.DoubleToLongFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.DoubleStream;
import java.util.stream.StreamSupport;

/**
 * The {@link Sequence} implementation specialized to primitive doubles.
 *
 * @author Eldar Abusalimov
 */
public class DoubleSequence extends AbstractList<Double> implements Sequence<Double> {
    private final double[] a;

    /**
     * Creates a new view into the specified array of doubles.
     *
     * @param array the non-null array of double values
     */
    public DoubleSequence(double[] array) {
        a = Objects.requireNonNull(array);
    }

    @Override
    public int size() {
        return a.length;
    }

    @Override
    public Double get(int index) {
        return a[index];
    }

    @Override
    public Double set(int index, Double element) {
        Double oldValue = a[index];
        a[index] = element;
        return oldValue;
    }

    @Override
    public Spliterator.OfDouble spliterator() {
        return Arrays.spliterator(a);
    }

    public DoubleStream doubleStream() {
        return StreamSupport.doubleStream(spliterator(), false);
    }

    public DoubleStream doubleParallelStream() {
        return StreamSupport.doubleStream(spliterator(), true);
    }

    public double reduce(double identity, DoubleBinaryOperator operator) {
        return doubleParallelStream().reduce(identity, operator);
    }

    @SuppressWarnings("unchecked")
    public <R> ObjectSequence<R> mapToObject(DoubleFunction<? extends R> mapper) {
        return new ObjectSequence(doubleParallelStream().mapToObj(mapper).toArray());
    }

    public LongSequence mapToLong(DoubleToLongFunction mapper) {
        return new LongSequence(doubleParallelStream().mapToLong(mapper).toArray());
    }

    public DoubleSequence mapToDouble(DoubleUnaryOperator mapper) {
        return new DoubleSequence(doubleParallelStream().map(mapper).toArray());
    }
}

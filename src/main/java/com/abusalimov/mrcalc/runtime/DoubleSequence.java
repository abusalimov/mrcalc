package com.abusalimov.mrcalc.runtime;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleFunction;
import java.util.function.DoubleToLongFunction;
import java.util.function.DoubleUnaryOperator;

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

    public double reduce(double identity, DoubleBinaryOperator operator) {
        return Arrays.stream(a).reduce(identity, operator);
    }

    @SuppressWarnings("unchecked")
    public <R> ObjectSequence<R> mapToObject(DoubleFunction<? extends R> mapper) {
        Object[] array = Arrays.stream(a).mapToObj(mapper).toArray();
        return new ObjectSequence(array);
    }

    public LongSequence mapToLong(DoubleToLongFunction mapper) {
        long[] array = Arrays.stream(a).mapToLong(mapper).toArray();
        return new LongSequence(array);
    }

    public DoubleSequence mapToDouble(DoubleUnaryOperator mapper) {
        double[] array = Arrays.stream(a).map(mapper).toArray();
        return new DoubleSequence(array);
    }
}

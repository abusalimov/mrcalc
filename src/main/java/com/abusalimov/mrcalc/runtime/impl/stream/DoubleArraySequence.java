package com.abusalimov.mrcalc.runtime.impl.stream;

import com.abusalimov.mrcalc.runtime.Sequence;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Spliterator;

/**
 * Implementation of specialized double sequence backed by an array of primitive doubles.
 *
 * @author Eldar Abusalimov
 */
public class DoubleArraySequence extends AbstractList<Double> implements Sequence.OfDouble {
    private final double[] a;

    /**
     * Creates a new view into the specified array of doubles.
     *
     * @param array the non-null array of double values
     */
    public DoubleArraySequence(double[] array) {
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
}

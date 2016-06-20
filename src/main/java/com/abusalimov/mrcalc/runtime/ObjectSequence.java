package com.abusalimov.mrcalc.runtime;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;

/**
 * @author Eldar Abusalimov
 */
public class ObjectSequence<E> extends AbstractList<E> implements Sequence<E> {
    private final E[] a;

    public ObjectSequence(E[] array) {
        a = Objects.requireNonNull(array);
    }

    @Override
    public int size() {
        return a.length;
    }

    @Override
    public E get(int index) {
        return a[index];
    }

    @Override
    public E set(int index, E element) {
        E oldValue = a[index];
        a[index] = element;
        return oldValue;
    }

    public E reduce(E identity, BinaryOperator<E> operator) {
        return Arrays.stream(a).reduce(identity, operator);
    }

    @SuppressWarnings("unchecked")
    public <R> ObjectSequence<R> mapToObject(Function<? super E, ? extends R> mapper) {
        Object[] array = Arrays.stream(a).map(mapper).toArray();
        return new ObjectSequence(array);
    }

    public LongSequence mapToLong(ToLongFunction<? super E> mapper) {
        long[] array = Arrays.stream(a).mapToLong(mapper).toArray();
        return new LongSequence(array);
    }

    public DoubleSequence mapToDouble(ToDoubleFunction<? super E> mapper) {
        double[] array = Arrays.stream(a).mapToDouble(mapper).toArray();
        return new DoubleSequence(array);
    }
}

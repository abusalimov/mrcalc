package com.abusalimov.mrcalc.runtime;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    @Override
    public Spliterator<E> spliterator() {
        return Arrays.spliterator(a);
    }

    @Override
    public Stream<E> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @Override
    public Stream<E> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }

    public E reduce(E identity, BinaryOperator<E> operator) {
        return parallelStream().reduce(identity, operator);
    }

    @SuppressWarnings("unchecked")
    public <R> ObjectSequence<R> mapToObject(Function<? super E, ? extends R> mapper) {
        return new ObjectSequence(parallelStream().map(mapper).toArray());
    }

    public LongSequence mapToLong(ToLongFunction<? super E> mapper) {
        return new LongSequence(parallelStream().mapToLong(mapper).toArray());
    }

    public DoubleSequence mapToDouble(ToDoubleFunction<? super E> mapper) {
        return new DoubleSequence(parallelStream().mapToDouble(mapper).toArray());
    }
}

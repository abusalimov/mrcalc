package com.abusalimov.mrcalc.runtime;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Spliterator;

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
}

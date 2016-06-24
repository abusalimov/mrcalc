package com.abusalimov.mrcalc.runtime.impl.stream;

import com.abusalimov.mrcalc.runtime.Sequence;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Spliterator;

/**
 * Implementation of generic sequence backed by an array of {@link Object}s.
 *
 * @author Eldar Abusalimov
 */
public class ObjectArraySequence<E> extends AbstractList<E> implements Sequence<E> {
    private final E[] a;

    public ObjectArraySequence(E[] array) {
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

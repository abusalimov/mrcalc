package com.abusalimov.mrcalc.runtime;

import java.util.List;
import java.util.RandomAccess;
import java.util.Spliterator;

/**
 * The marker interface for object representing a sequence of elements of arbitrary types.
 *
 * @param <E> the type of the sequence elements
 * @author Eldar Abusalimov
 */
public interface Sequence<E> extends List<E>, RandomAccess {
    @Override
    Spliterator<E> spliterator();
}

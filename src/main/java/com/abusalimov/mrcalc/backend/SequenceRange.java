package com.abusalimov.mrcalc.backend;

/**
 * Provides a {@link #range(Object, Object)} method} for creating expressions constructing ranges of integers.
 *
 * @param <S> the type of an expression corresponding to a range sequence being constructed; used by the implementation
 * @param <E> the type of an expression corresponding to integer elements of a source sequence; used by the
 *            implementation
 * @author Eldar Abusalimov
 */
public interface SequenceRange<S, E> {
    /**
     * Creates an expression yielding a range object with its boundaries calculated by evaluating the specified start
     * and stop expressions.
     *
     * @param start the expression yielding a value of the start boundary of the range
     * @param end   the expression yielding a value of the end boundary of the range
     * @return the expression creating a new range sequence
     */
    S range(E start, E end);
}

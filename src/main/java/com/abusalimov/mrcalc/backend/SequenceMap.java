package com.abusalimov.mrcalc.backend;

/**
 * Provides a {@link #map(Object, Object) method} for creating expressions performing sequence map().
 *
 * @param <D> the type of an expression corresponding to a resulting destination sequence; used by the implementation
 * @param <S> the type of an expression corresponding to a source sequence; used by the implementation
 * @param <L> the function type corresponding to a mapper lambda; used by the implementation
 * @author Eldar Abusalimov
 */
public interface SequenceMap<D, S, L> {
    /**
     * Creates an expression mapping the result of the specified sequence expression using the given lambda.
     *
     * @param sequence the expression yielding a sequence to map
     * @param lambda   the expression to use for transforming each element of the sequence
     * @return the expression performing the map() logic
     */
    D map(S sequence, L lambda);
}

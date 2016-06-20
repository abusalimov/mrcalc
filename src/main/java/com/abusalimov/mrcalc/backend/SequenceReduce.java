package com.abusalimov.mrcalc.backend;

/**
 * Provides a {@link #reduce(Object, Object, Object) method} for creating expressions performing sequence reduce().
 *
 * @param <E> the type of expressions corresponding to elements of a source sequence, a neutral element and a resulting
 *            reduction value; used by the implementation
 * @param <S> the type of an expression corresponding to a source sequence; used by the implementation
 * @param <L> the function type corresponding to an accumulator lambda; used by the implementation
 */
public interface SequenceReduce<E, S, L> {
    /**
     * Creates an expression reducing the result of the specified sequence expression using the given lambda and
     * starting from the result of neutral expression.
     *
     * @param sequence the expression yielding a sequence to reduce
     * @param neutral  the expression yielding a neutral element
     * @param lambda   the function stub to use to combine an accumulated return value with each element of the
     *                 sequence
     * @return the expression performing the reduce() logic
     */
    E reduce(S sequence, E neutral, L lambda);
}

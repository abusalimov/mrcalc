package com.abusalimov.mrcalc.runtime;

import java.util.function.*;

/**
 * The runtime provides the necessary support functions (mainly for map() / reduce()) used for evaluating an expression.
 * It also serves as a factory for certain types of objects, like {@link #createLongRange(long, long) ranges}.
 *
 * @author Eldar Abusalimov
 */
public class Runtime {
    /**
     * Creates a new {@link LongSequence} filled by integers between the specified boundaries.
     *
     * @param startInclusive the start boundary of the range (inclusive)
     * @param endExclusive   the end boundary of the range (exclusive)
     * @return the new {@link LongSequence} instance
     */
    public LongSequence createLongRange(long startInclusive, long endExclusive) {
        return new LongRange(startInclusive, endExclusive);
    }

    public <E> E reduce(ObjectSequence<E> sequence, E identity, BinaryOperator<E> operator) {
        return sequence.parallelStream().reduce(identity, operator);
    }

    public long reduce(LongSequence sequence, long identity, LongBinaryOperator operator) {
        return sequence.longParallelStream().reduce(identity, operator);
    }

    public double reduce(DoubleSequence sequence, double identity, DoubleBinaryOperator operator) {
        return sequence.doubleParallelStream().reduce(identity, operator);
    }

    @SuppressWarnings("unchecked")
    public <E, R> ObjectSequence<R> mapToObject(ObjectSequence<E> sequence, Function<? super E, ? extends R> mapper) {
        return new ObjectSequence(sequence.parallelStream().map(mapper).toArray());
    }

    @SuppressWarnings("unchecked")
    public <R> ObjectSequence<R> mapToObject(LongSequence sequence, LongFunction<? extends R> mapper) {
        return new ObjectSequence(sequence.longParallelStream().mapToObj(mapper).toArray());
    }

    @SuppressWarnings("unchecked")
    public <R> ObjectSequence<R> mapToObject(DoubleSequence sequence, DoubleFunction<? extends R> mapper) {
        return new ObjectSequence(sequence.doubleParallelStream().mapToObj(mapper).toArray());
    }

    public <E> LongSequence mapToLong(ObjectSequence<E> sequence, ToLongFunction<? super E> mapper) {
        return new LongSequence(sequence.parallelStream().mapToLong(mapper).toArray());
    }

    public LongSequence mapToLong(LongSequence sequence, LongUnaryOperator mapper) {
        return new LongSequence(sequence.longParallelStream().map(mapper).toArray());
    }

    public LongSequence mapToLong(DoubleSequence sequence, DoubleToLongFunction mapper) {
        return new LongSequence(sequence.doubleParallelStream().mapToLong(mapper).toArray());
    }

    public <E> DoubleSequence mapToDouble(ObjectSequence<E> sequence, ToDoubleFunction<? super E> mapper) {
        return new DoubleSequence(sequence.parallelStream().mapToDouble(mapper).toArray());
    }

    public DoubleSequence mapToDouble(LongSequence sequence, LongToDoubleFunction mapper) {
        return new DoubleSequence(sequence.longParallelStream().mapToDouble(mapper).toArray());
    }

    public DoubleSequence mapToDouble(DoubleSequence sequence, DoubleUnaryOperator mapper) {
        return new DoubleSequence(sequence.doubleParallelStream().map(mapper).toArray());
    }

}

package com.abusalimov.mrcalc.runtime;

import java.util.function.*;
import java.util.stream.DoubleStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * The runtime provides the necessary support functions (mainly for map() / reduce()) used for evaluating an expression.
 * It also serves as a factory for certain types of objects, like {@link #createLongRange(long, long) ranges}.
 *
 * @author Eldar Abusalimov
 */
public class Runtime {
    private final boolean parallel;

    /**
     * Creates a new instance providing functions executing in parallel, where possible.
     */
    public Runtime() {
        this(true);
    }

    /**
     * Creates a new instance operating in parallel, as indicated by the argument.
     *
     * @param parallel whether to use parallel operations, where possible, or not
     */
    public Runtime(boolean parallel) {
        this.parallel = parallel;
    }

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
        return objectStream(sequence).reduce(identity, operator);
    }

    public long reduce(LongSequence sequence, long identity, LongBinaryOperator operator) {
        return longStream(sequence).reduce(identity, operator);
    }

    public double reduce(DoubleSequence sequence, double identity, DoubleBinaryOperator operator) {
        return doubleStream(sequence).reduce(identity, operator);
    }

    @SuppressWarnings("unchecked")
    public <E, R> ObjectSequence<R> mapToObject(ObjectSequence<E> sequence, Function<? super E, ? extends R> mapper) {
        return new ObjectSequence(objectStream(sequence).map(mapper).toArray());
    }

    @SuppressWarnings("unchecked")
    public <R> ObjectSequence<R> mapToObject(LongSequence sequence, LongFunction<? extends R> mapper) {
        return new ObjectSequence(longStream(sequence).mapToObj(mapper).toArray());
    }

    @SuppressWarnings("unchecked")
    public <R> ObjectSequence<R> mapToObject(DoubleSequence sequence, DoubleFunction<? extends R> mapper) {
        return new ObjectSequence(doubleStream(sequence).mapToObj(mapper).toArray());
    }

    public <E> LongSequence mapToLong(ObjectSequence<E> sequence, ToLongFunction<? super E> mapper) {
        return new LongSequence(objectStream(sequence).mapToLong(mapper).toArray());
    }

    public LongSequence mapToLong(LongSequence sequence, LongUnaryOperator mapper) {
        return new LongSequence(longStream(sequence).map(mapper).toArray());
    }

    public LongSequence mapToLong(DoubleSequence sequence, DoubleToLongFunction mapper) {
        return new LongSequence(doubleStream(sequence).mapToLong(mapper).toArray());
    }

    public <E> DoubleSequence mapToDouble(ObjectSequence<E> sequence, ToDoubleFunction<? super E> mapper) {
        return new DoubleSequence(objectStream(sequence).mapToDouble(mapper).toArray());
    }

    public DoubleSequence mapToDouble(LongSequence sequence, LongToDoubleFunction mapper) {
        return new DoubleSequence(longStream(sequence).mapToDouble(mapper).toArray());
    }

    public DoubleSequence mapToDouble(DoubleSequence sequence, DoubleUnaryOperator mapper) {
        return new DoubleSequence(doubleStream(sequence).map(mapper).toArray());
    }

    protected <E> Stream<E> objectStream(ObjectSequence<E> sequence) {
        return StreamSupport.stream(sequence.spliterator(), parallel);
    }

    protected LongStream longStream(LongSequence sequence) {
        return StreamSupport.longStream(sequence.spliterator(), parallel);
    }

    protected DoubleStream doubleStream(DoubleSequence sequence) {
        return StreamSupport.doubleStream(sequence.spliterator(), parallel);
    }

}

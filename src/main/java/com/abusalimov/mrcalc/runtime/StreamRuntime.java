package com.abusalimov.mrcalc.runtime;

import java.util.function.*;
import java.util.stream.DoubleStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * The runtime implementation using Java 8 {@link Stream} API.
 *
 * @author Eldar Abusalimov
 */
public class StreamRuntime implements Runtime {
    private final boolean parallel;

    /**
     * Creates a new instance providing functions executing in parallel, where possible.
     */
    public StreamRuntime() {
        this(true);
    }

    /**
     * Creates a new instance operating in parallel, as indicated by the argument.
     *
     * @param parallel whether to use parallel operations, where possible, or not
     */
    public StreamRuntime(boolean parallel) {
        this.parallel = parallel;
    }

    @Override
    public LongSequence createLongRange(long startInclusive, long endExclusive) {
        return new LongRange(startInclusive, endExclusive);
    }

    @Override
    public <E> E reduce(Sequence<E> sequence, E identity, BinaryOperator<E> operator) {
        return objectStream(sequence).reduce(identity, operator);
    }

    @Override
    public long reduceLong(LongSequence sequence, long identity, LongBinaryOperator operator) {
        return longStream(sequence).reduce(identity, operator);
    }

    @Override
    public double reduceDouble(DoubleSequence sequence, double identity, DoubleBinaryOperator operator) {
        return doubleStream(sequence).reduce(identity, operator);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E, R> Sequence<R> mapToObject(Sequence<E> sequence, Function<? super E, ? extends R> mapper) {
        return new ObjectSequence(objectStream(sequence).map(mapper).toArray());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> Sequence<R> mapLongToObject(LongSequence sequence, LongFunction<? extends R> mapper) {
        return new ObjectSequence(longStream(sequence).mapToObj(mapper).toArray());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> Sequence<R> mapDoubleToObject(DoubleSequence sequence, DoubleFunction<? extends R> mapper) {
        return new ObjectSequence(doubleStream(sequence).mapToObj(mapper).toArray());
    }

    @Override
    public <E> LongSequence mapToLong(Sequence<E> sequence, ToLongFunction<? super E> mapper) {
        return new LongSequence(objectStream(sequence).mapToLong(mapper).toArray());
    }

    @Override
    public LongSequence mapLongToLong(LongSequence sequence, LongUnaryOperator mapper) {
        return new LongSequence(longStream(sequence).map(mapper).toArray());
    }

    @Override
    public LongSequence mapDoubleToLong(DoubleSequence sequence, DoubleToLongFunction mapper) {
        return new LongSequence(doubleStream(sequence).mapToLong(mapper).toArray());
    }

    @Override
    public <E> DoubleSequence mapToDouble(Sequence<E> sequence, ToDoubleFunction<? super E> mapper) {
        return new DoubleSequence(objectStream(sequence).mapToDouble(mapper).toArray());
    }

    @Override
    public DoubleSequence mapLongToDouble(LongSequence sequence, LongToDoubleFunction mapper) {
        return new DoubleSequence(longStream(sequence).mapToDouble(mapper).toArray());
    }

    @Override
    public DoubleSequence mapDoubleToDouble(DoubleSequence sequence, DoubleUnaryOperator mapper) {
        return new DoubleSequence(doubleStream(sequence).map(mapper).toArray());
    }

    protected <E> Stream<E> objectStream(Sequence<E> sequence) {
        return StreamSupport.stream(sequence.spliterator(), parallel);
    }

    protected LongStream longStream(LongSequence sequence) {
        return StreamSupport.longStream(sequence.spliterator(), parallel);
    }

    protected DoubleStream doubleStream(DoubleSequence sequence) {
        return StreamSupport.doubleStream(sequence.spliterator(), parallel);
    }

}

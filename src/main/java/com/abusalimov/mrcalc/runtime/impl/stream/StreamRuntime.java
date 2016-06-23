package com.abusalimov.mrcalc.runtime.impl.stream;

import com.abusalimov.mrcalc.runtime.DoubleSequence;
import com.abusalimov.mrcalc.runtime.LongSequence;
import com.abusalimov.mrcalc.runtime.Runtime;
import com.abusalimov.mrcalc.runtime.Sequence;

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
    public LongArraySequence createLongRange(long startInclusive, long endExclusive) {
        return new LongArrayRange(startInclusive, endExclusive);
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
        return new ObjectArraySequence(objectStream(sequence).map(mapper).toArray());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> Sequence<R> mapLongToObject(LongSequence sequence, LongFunction<? extends R> mapper) {
        return new ObjectArraySequence(longStream(sequence).mapToObj(mapper).toArray());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> Sequence<R> mapDoubleToObject(DoubleSequence sequence, DoubleFunction<? extends R> mapper) {
        return new ObjectArraySequence(doubleStream(sequence).mapToObj(mapper).toArray());
    }

    @Override
    public <E> LongArraySequence mapToLong(Sequence<E> sequence, ToLongFunction<? super E> mapper) {
        return new LongArraySequence(objectStream(sequence).mapToLong(mapper).toArray());
    }

    @Override
    public LongArraySequence mapLongToLong(LongSequence sequence, LongUnaryOperator mapper) {
        return new LongArraySequence(longStream(sequence).map(mapper).toArray());
    }

    @Override
    public LongArraySequence mapDoubleToLong(DoubleSequence sequence, DoubleToLongFunction mapper) {
        return new LongArraySequence(doubleStream(sequence).mapToLong(mapper).toArray());
    }

    @Override
    public <E> DoubleArraySequence mapToDouble(Sequence<E> sequence, ToDoubleFunction<? super E> mapper) {
        return new DoubleArraySequence(objectStream(sequence).mapToDouble(mapper).toArray());
    }

    @Override
    public DoubleArraySequence mapLongToDouble(LongSequence sequence, LongToDoubleFunction mapper) {
        return new DoubleArraySequence(longStream(sequence).mapToDouble(mapper).toArray());
    }

    @Override
    public DoubleArraySequence mapDoubleToDouble(DoubleSequence sequence, DoubleUnaryOperator mapper) {
        return new DoubleArraySequence(doubleStream(sequence).map(mapper).toArray());
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

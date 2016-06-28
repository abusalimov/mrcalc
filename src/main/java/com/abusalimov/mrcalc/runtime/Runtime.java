package com.abusalimov.mrcalc.runtime;

import java.util.function.*;

/**
 * The runtime provides the necessary support functions (mainly for map() / reduce()) used for evaluating an expression.
 * It also serves as a factory for certain types of objects, like {@link #createLongRange(long, long) ranges}.
 *
 * The {@link Runtime} implementation only accepts
 *
 * @author Eldar Abusalimov
 */
@SuppressWarnings("unused")  // may be used through reflection / dynamically generated code
public interface Runtime {
    /**
     * Creates a new {@link Sequence.OfLong} filled by integers between the specified boundaries.
     *
     * @param startInclusive the start boundary of the range (inclusive)
     * @param endExclusive   the end boundary of the range (exclusive)
     * @return the new {@link Sequence.OfLong} instance
     */
    Sequence.OfLong createLongRange(long startInclusive, long endExclusive);

    /**
     * Creates a new {@link Sequence.OfLong} filled by integers between the specified boundaries.
     *
     * @param startInclusive the start boundary of the range (inclusive)
     * @param endInclusive   the end boundary of the range (inclusive)
     * @return the new {@link Sequence.OfLong} instance
     */
    default Sequence.OfLong createLongRangeInclusive(long startInclusive, long endInclusive) {
        return createLongRange(startInclusive, endInclusive + 1);
    }

    /**
     * Performs a reduction on the elements of the given sequence, using the provided identity value and an associative
     * accumulation function, and returns the reduced value.
     * <p>
     * All values involved into the reduction, as well as the return type, are treated as generic objects, and
     * primitives must be boxed appropriately. In case of using a sequence of primitives, to achieve better performance,
     * a specialized method should be considered instead.
     *
     * @param sequence the sequence of objects to reduce
     * @param identity the neutral element, which must be an identity for the operator
     * @param operator the accumulator function: {@code E, E -> E}
     * @param <E>      a type of the values, involved into the reduction as well as the return type
     * @return the result of the reduction
     * @see #reduceLong(Sequence.OfLong, long, LongBinaryOperator) for reduction of the primitive longs
     * @see #reduceDouble(Sequence.OfDouble, double, DoubleBinaryOperator) for reduction of the primitive doubles
     */
    <E> E reduce(Sequence<E> sequence, E identity, BinaryOperator<E> operator);

    /**
     * Performs a reduction on the elements of the given sequence of primitive longs, using the provided long identity
     * value and an associative accumulation function, and returns the reduced value.
     *
     * @param sequence the sequence of primitive longs to reduce
     * @param identity the neutral element, which must be an identity for the operator
     * @param operator the accumulator function: {@code long, long -> long}
     * @return the result of the reduction
     */
    long reduceLong(Sequence.OfLong sequence, long identity, LongBinaryOperator operator);

    /**
     * Performs a reduction on the elements of the given sequence of primitive doubles, using the provided double
     * identity value and an associative accumulation function, and returns the reduced value.
     *
     * @param sequence the sequence of primitive doubles to reduce
     * @param identity the neutral element, which must be an identity for the operator
     * @param operator the accumulator function: {@code double, double -> double}
     * @return the result of the reduction
     */
    double reduceDouble(Sequence.OfDouble sequence, double identity, DoubleBinaryOperator operator);

    /**
     * Returns a sequence consisting of the results of applying the given function to the elements of the specified
     * sequence.
     * <p>
     * All values are treated as generic objects, and primitives must be boxed appropriately. In case of using a
     * sequence of primitives, to achieve better performance, a specialized method should be considered instead.
     *
     * @param sequence the sequence of objects to map
     * @param mapper   the function to apply for each element: {@code E -> R}
     * @param <E>      the element type of the source sequence
     * @param <R>      the element type of the destination sequence
     * @return the new sequence of objects
     * @see #mapLongToObject(Sequence.OfLong, LongFunction) for mapping sequences of primitive longs
     * @see #mapDoubleToObject(Sequence.OfDouble, DoubleFunction) for mapping sequences of primitive doubles
     */
    <E, R> Sequence<R> mapToObject(Sequence<E> sequence, Function<? super E, ? extends R> mapper);

    /**
     * Returns a sequence consisting of the results of applying the given function to the elements of the specified
     * sequence of primitive longs.
     *
     * @param sequence the sequence of primitive longs to map
     * @param mapper   the function to apply for each element: {@code long -> R}
     * @param <R>      the element type of the destination sequence
     * @return the sequence of objects
     */
    <R> Sequence<R> mapLongToObject(Sequence.OfLong sequence, LongFunction<? extends R> mapper);

    /**
     * Returns a sequence consisting of the results of applying the given function to the elements of the specified
     * sequence of primitive doubles.
     *
     * @param sequence the sequence of primitive doubles to map
     * @param mapper   the function to apply for each element: {@code double -> R}
     * @param <R>      the element type of the destination sequence
     * @return the sequence of objects
     */
    <R> Sequence<R> mapDoubleToObject(Sequence.OfDouble sequence, DoubleFunction<? extends R> mapper);

    /**
     * Returns a sequence of primitive longs consisting of the results of applying the given function to the elements of
     * the specified sequence.
     * <p>
     * The elements of the source sequence are treated as generic objects, and primitives must be boxed appropriately.
     * In case of using a sequence of primitives, to achieve better performance, a specialized method should be
     * considered instead.
     *
     * @param sequence the sequence of objects to map
     * @param mapper   the function to apply for each element: {@code E -> long}
     * @param <E>      the element type of the source sequence
     * @return the sequence of primitive longs
     * @see #mapLongToLong(Sequence.OfLong, LongUnaryOperator) for mapping sequences of primitive longs
     * @see #mapDoubleToLong(Sequence.OfDouble, DoubleToLongFunction) for mapping sequences of primitive doubles
     */
    <E> Sequence.OfLong mapToLong(Sequence<E> sequence, ToLongFunction<? super E> mapper);

    /**
     * Returns a sequence of primitive longs consisting of the results of applying the given function to the elements of
     * the specified sequence of primitive longs.
     *
     * @param sequence the sequence of primitive longs to map
     * @param mapper   the function to apply for each element: {@code long -> long}
     * @return the sequence of primitive longs
     */
    Sequence.OfLong mapLongToLong(Sequence.OfLong sequence, LongUnaryOperator mapper);

    /**
     * Returns a sequence of primitive longs consisting of the results of applying the given function to the elements of
     * the specified sequence of primitive doubles.
     *
     * @param sequence the sequence of primitive doubles to map
     * @param mapper   the function to apply for each element: {@code double -> long}
     * @return the sequence of primitive longs
     */
    Sequence.OfLong mapDoubleToLong(Sequence.OfDouble sequence, DoubleToLongFunction mapper);

    /**
     * Returns a sequence of primitive doubles consisting of the results of applying the given function to the elements
     * of the specified sequence.
     * <p>
     * The elements of the source sequence are treated as generic objects, and primitives must be boxed appropriately.
     * In case of using a sequence of primitives, to achieve better performance, a specialized method should be
     * considered instead.
     *
     * @param sequence the sequence of objects to map
     * @param mapper   the function to apply for each element: {@code E -> double}
     * @param <E>      the element type of the source sequence
     * @return the sequence of primitive doubles
     * @see #mapLongToDouble(Sequence.OfLong, LongToDoubleFunction) for mapping sequences of primitive longs
     * @see #mapDoubleToDouble(Sequence.OfDouble, DoubleUnaryOperator) for mapping sequences of primitive doubles
     */
    <E> Sequence.OfDouble mapToDouble(Sequence<E> sequence, ToDoubleFunction<? super E> mapper);

    /**
     * Returns a sequence of primitive doubles consisting of the results of applying the given function to the elements
     * of the specified sequence of primitive longs.
     *
     * @param sequence the sequence of primitive longs to map
     * @param mapper   the function to apply for each element: {@code long -> double}
     * @return the sequence of primitive doubles
     */
    Sequence.OfDouble mapLongToDouble(Sequence.OfLong sequence, LongToDoubleFunction mapper);

    /**
     * Returns a sequence of primitive doubles consisting of the results of applying the given function to the elements
     * of the specified sequence of primitive doubles.
     *
     * @param sequence the sequence of primitive doubles to map
     * @param mapper   the function to apply for each element: {@code double -> double}
     * @return the sequence of primitive doubles
     */
    Sequence.OfDouble mapDoubleToDouble(Sequence.OfDouble sequence, DoubleUnaryOperator mapper);

    /**
     * Provides the power math operation methods required since Java doesn't have a builtin one.
     */
    final class Util {
        /**
         * The private constructor.
         */
        private Util() {
        }

        /**
         * Computes the result of raising the first argument to the power of the second argument.
         *
         * @param a the base
         * @param b the exponent
         * @return a ^ b
         */
        public static long powLong(long a, long b) {
            double ret = Math.pow(a, b);
            if (!Double.isFinite(ret)) {
                throw new ArithmeticException(a + " ^ " + b);
            }
            return (long) ret;
        }

        /**
         * Computes the result of raising the first argument to the power of the second argument.
         *
         * @param a the base
         * @param b the exponent
         * @return a ^ b
         */
        public static double powDouble(double a, double b) {
            return Math.pow(a, b);
        }
    }
}

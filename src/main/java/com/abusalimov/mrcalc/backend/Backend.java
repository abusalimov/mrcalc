package com.abusalimov.mrcalc.backend;

/**
 * The backend is responsible for creating callable functions from basic expressions.
 *
 * @param <E> the type of expressions used by the backend implementation
 * @param <F> the internal type of assembled functions used by the backend implementation
 * @author Eldar Abusalimov
 */
public interface Backend<E, F> {
    /**
     * Creates a new function assembler for a function with given signature.
     *
     * @param returnType     the return type of the function to be assembled
     * @param parameterTypes the types of parameters taken by the function to be assembled
     * @param <R>            the return type
     * @return the new {@link FunctionAssembler} instance
     */
    <R> FunctionAssembler<R, E, F> createFunctionAssembler(Class<R> returnType, Class<?>... parameterTypes);

    /**
     * Gets a {@link NumberMath} instance suitable for assembling expressions of the given numeric type.
     *
     * @param returnType the required class of numeric expressions being assembled
     * @param <T>        the class type argument
     * @return the {@link NumberMath} instance
     */
    <T extends Number> NumberMath<T, E> getNumberMath(Class<T> returnType);

    /**
     * Gets a {@link NumberCast} instance providing a method for numeric conversion between the specified types.
     *
     * @param toType   the result type of the required conversion
     * @param fromType the original type of the conversion
     * @return the {@link NumberCast} instance for numeric conversions
     */
    NumberCast<E, E> getNumberCast(Class<? extends Number> toType, Class<? extends Number> fromType);

    /**
     * Returns a {@link SequenceRange} instance suitable for assembling expressions creating a range of integers of the
     * given type.
     *
     * @param elementType the integer type of the range elements
     * @return the {@link SequenceRange} instance
     */
    SequenceRange<E, E> getSequenceRange(Class<? extends Number> elementType);

    /**
     * Returns a {@link SequenceReduce} instance suitable for assembling a sequence reduction operation for the given
     * type of the sequence element and the resulting value.
     *
     * @param returnType the result type of the reduction to be assembled
     * @return the {@link SequenceReduce} instance
     */
    SequenceReduce<E, E, E> getSequenceReduce(Class<?> returnType);

    /**
     * Returns a {@link SequenceMap} instance suitable for assembling a sequence mapping operation for the given types
     * of elements the source and destination sequences.
     *
     * @param returnElementType the type of elements of the resulting sequence
     * @param elementType       the type of elements of the source sequence
     * @return the {@link SequenceMap} instance
     */
    SequenceMap<E, E, E> getSequenceMap(Class<?> returnElementType, Class<?> elementType);

}

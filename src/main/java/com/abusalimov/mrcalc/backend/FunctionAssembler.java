package com.abusalimov.mrcalc.backend;

import com.abusalimov.mrcalc.runtime.Evaluable;

/**
 * The function assembler is responsible for building an expression tree into a callable function.
 *
 * @param <R> the return type of the function constructed using this assembler
 * @param <E> the expression type used by the implementation
 * @param <F> the type of a function constructed and used internally by the backend implementation
 * @author Eldar Abusalimov
 */
public interface FunctionAssembler<R, E, F> {
    /**
     * Returns an {@link ArgumentLoad} instance capable for creating expressions loading an argument of the given
     * parameter type.
     *
     * @param parameterType the type of the value to be loaded from the argument
     * @return the {@link ArgumentLoad} instance
     */
    ArgumentLoad<E> getArgumentLoad(Class<?> parameterType);

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

    /**
     * Creates a function that can be used to construct a {@link #lambda(Object)} or {@link Evaluable} later on.
     *
     * @param expr the resulting expression
     * @return an object representing the function evaluating the expression
     */
    F assemble(E expr);

    /**
     * Registers a given lambda function and creates an expression loading an instance that lambda.
     *
     * @param function the lambda to register
     * @return the expression loading a value of the specified variable
     */
    E lambda(F function);

    /**
     * Completes the assembling and returns an {@link Evaluable} function accepting an array of values of the referenced
     * variables and returning the results of evaluating the expression.
     * <p>
     * This method is called to build up an expression statement. It is not used for lambdas.
     *
     * @param function the function {@link #assemble(Object) assembled} using this function assembler
     * @return the {@link Evaluable} wrapping the call to the function
     */
    Evaluable<R> toEvaluable(F function);
}

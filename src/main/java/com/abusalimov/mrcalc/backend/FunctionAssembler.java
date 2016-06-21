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

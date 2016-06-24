package com.abusalimov.mrcalc.backend;

/**
 * The backend is responsible for creating callable functions from basic expressions.
 *
 * This class is the main entry point for the backend and serves as a factory for a {@link FunctionAssembler}, which is
 * responsible for assembling {@link com.abusalimov.mrcalc.runtime.Evaluable} functions.
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
}

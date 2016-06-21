package com.abusalimov.mrcalc.backend;

import com.abusalimov.mrcalc.runtime.Evaluable;

/**
 * The base backend class for assembling expressions.
 * <p>
 * Provides factory methods for creating expressions for operations applicable to all types.
 *
 * @param <T> the base type of values involved in or returned by the expressions constructed using this factory
 * @param <E> the main expression type used by the implementation
 * @author Eldar Abusalimov
 */
public interface ObjectMath<T, E> {
    /**
     * Completes the assembling and returns a callable function accepting an array of values of the referenced variables
     * and returning the results of evaluating the expression.
     * <p>
     * This method is called to build up an expression statement. It is not used for lambdas.
     *
     * @param expr the expression to assemble
     * @return the callable function evaluating the expression
     */
    Evaluable<?> toEvaluable(E expr);
}

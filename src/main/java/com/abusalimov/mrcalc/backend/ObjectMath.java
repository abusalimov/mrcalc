package com.abusalimov.mrcalc.backend;

import com.abusalimov.mrcalc.runtime.Evaluable;

/**
 * The base backend class for assembling expressions.
 * <p>
 * Provides factory methods for creating expressions for operations applicable to all types.
 *
 * @param <T> the base type of values involved in or returned by the expressions constructed using this factory
 * @param <E> the main expression type used by the implementation
 * @param <F> the auxiliary expression type used to emphasize the distinction with the base expression type and ensure
 *            type safety. This is likely to identical to the E type upon generic instantiation
 * @author Eldar Abusalimov
 */
public interface ObjectMath<T, E, F> {
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

    /**
     * Creates an expression accessing a variable at the specified index.
     *
     * @param slot the variable index
     * @return the expression loading a value of the specified variable
     */
    E load(int slot);

    /**
     * Creates an expression yielding the specified constant.
     *
     * @param literal the constant to return from the expression
     * @return the expression returning the constant value
     */
    E constant(T literal);

    /**
     * Creates an expression mapping the result of the specified sequence expression using the given lambda.
     *
     * @param sequence the expression yielding a sequence to map
     * @param lambda   the expression to use for transforming each element of the sequence
     * @return the expression performing the map() logic
     */
    F map(F sequence, E lambda);

    /**
     * Creates an expression reducing the result of the specified sequence expression using the given lambda and
     * starting from the result of neutral expression.
     *
     * @param sequence the expression yielding a sequence to reduce
     * @param neutral  the expression yielding a neutral element
     * @param lambda   the expression to use to combine an accumulated return value with each element of the sequence
     * @return the expression performing the reduce() logic
     */
    E reduce(F sequence, E neutral, E lambda);

}

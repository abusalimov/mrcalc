package com.abusalimov.mrcalc.backend;

/**
 * The backend class for assembling numeric expressions.
 * <p>
 * Provides extended set of factory methods for creating expressions implementing the numeric math.
 *
 * @param <T> the base numeric type operated on by the expressions constructed using this factory
 * @param <E> the main expression type used by the implementation
 * @param <F> the auxiliary expression type used to emphasize the distinction with the base expression type and ensure
 *            type safety. This is likely to identical to the E type upon generic instantiation
 * @author Eldar Abusalimov
 */
public interface NumberMath<T extends Number, E, F> extends ObjectMath<T, E, F> {
    /* The basic Math operations, the semantics should be obvious. */

    /** Expression adding the results of evaluating two operand expressions together. */
    E add(E leftOperand, E rightOperand);

    /** Expression subtracting the result of evaluating the right operand expressions from the left one. */
    E sub(E leftOperand, E rightOperand);

    /** Expression multiplying the results of evaluating two operand expressions. */
    E mul(E leftOperand, E rightOperand);

    /** Expression dividing the result of evaluating the left operand expressions by the right one. */
    E div(E leftOperand, E rightOperand);

    /** Expression raising the result of evaluating the left operand expressions by the power of the right one. */
    E pow(E leftOperand, E rightOperand);

    /** Expression negating the result of evaluating the operand expression. */
    E neg(E operand);

    /**
     * Creates an expression yielding a range object with its boundaries calculated by evaluating the specified start
     * and stop expressions.
     *
     * @param start the expression yielding a value of the start boundary of the range
     * @param end   the expression yielding a value of the end boundary of the range
     * @return the expression creating a new range sequence
     */
    F range(E start, E end);
}

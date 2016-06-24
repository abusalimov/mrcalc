package com.abusalimov.mrcalc.backend;

/**
 * Provides a single method for assembling a numeric conversion between certain types.
 *
 * @param <E> the target type of the conversion
 * @param <F> the source type parameter of the numeric conversion performed by the expressions created using this
 *            factory
 * @author Eldar Abusalimov
 */
public interface NumberCast<E, F> {
    /**
     * Wraps the specified expression to make it yield a value of another numeric type.
     *
     * @param expr the expression yielding a value to convert
     * @return an expression of the converted type
     */
    E cast(F expr);
}

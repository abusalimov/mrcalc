package com.abusalimov.mrcalc.backend;

/**
 * Provides a single method for assembling a numeric conversion between certain types.
 *
 * @param <F> the source type parameter of the numeric conversion performed by the expressions created using this
 *            factory
 * @param <E> the target type of the conversion
 * @author Eldar Abusalimov
 */
public interface NumberCast<F, E> {
    /**
     * Wraps the specified expression to make it yield a value of another numeric type.
     *
     * @param expr the expression yielding a value to convert
     * @return an expression of the converted type
     */
    E cast(F expr);
}

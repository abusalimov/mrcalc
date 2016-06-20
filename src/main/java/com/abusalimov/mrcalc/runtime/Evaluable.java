package com.abusalimov.mrcalc.runtime;

/**
 * The entry point interface for assembled expressions produced by the backend.
 *
 * @author Eldar Abusalimov
 */
public interface Evaluable<T> {
    /**
     * Evaluates the expression with given arguments and using the specified {@link Runtime} instance.
     * <p>
     * Generally, the arguments correspond to global variables referenced from within the expression. The caller must
     * ensure to pass the arguments of proper types and packed in the proper order, otherwise an unchecked {@link
     * RuntimeException} may be thrown (although it is not guaranteed to).
     *
     * @param runtime the non-null Runtime instance
     * @param args    the array of arguments
     * @return the result of evaluating the expression
     */
    T eval(Runtime runtime, Object... args);
}

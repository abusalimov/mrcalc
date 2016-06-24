package com.abusalimov.mrcalc.backend;

/**
 * Provides a {@link #load(int) method} for creating expressions loading a function argument value.
 *
 * @param <E> the expression type used by the implementation
 * @author Eldar Abusalimov
 */
public interface ArgumentLoad<E> {
    /**
     * Creates an expression accessing a variable at the specified index.
     *
     * @param slot the variable index
     * @return the expression loading a value of the specified variable
     */
    E load(int slot);
}

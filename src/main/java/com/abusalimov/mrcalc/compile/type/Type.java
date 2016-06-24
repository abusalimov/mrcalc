package com.abusalimov.mrcalc.compile.type;

/**
 * Represents the type of an expression inferred using the type inference rules.
 *
 * @author Eldar Abusalimov
 */
public interface Type {
    /**
     * Returns the {@link PrimitiveType} itself for primitives, or the primitive type of the innermost element in case of
     * sequence types.
     *
     * @return the underlying scalar type
     */
    PrimitiveType getPrimitiveType();

    /**
     * Returns a Java class corresponding to this type.
     *
     * @return the backing Java class
     */
    Class<?> getTypeClass();
}

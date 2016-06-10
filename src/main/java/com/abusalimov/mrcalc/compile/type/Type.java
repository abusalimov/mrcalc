package com.abusalimov.mrcalc.compile.type;

/**
 * @author Eldar Abusalimov
 */
public interface Type {
    Primitive getPrimitive();

    Class<?> getTypeClass();
}

package com.abusalimov.mrcalc.compile.exprtree;

/**
 * @author Eldar Abusalimov
 */
public interface Expr<T extends Number> {
    Expr INVALID = () -> Type.UNKNOWN;

    Type getType();
}

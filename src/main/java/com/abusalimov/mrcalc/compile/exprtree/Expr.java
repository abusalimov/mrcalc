package com.abusalimov.mrcalc.compile.exprtree;

/**
 * @author Eldar Abusalimov
 */
public interface Expr {
    Expr INVALID = () -> Type.UNKNOWN;

    Type getType();

    default Expr cast(Type type) {
        return INVALID;
    }
}

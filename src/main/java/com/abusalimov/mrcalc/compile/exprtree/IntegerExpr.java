package com.abusalimov.mrcalc.compile.exprtree;

/**
 * @author Eldar Abusalimov
 */
public interface IntegerExpr extends Expr<Long> {
    @Override
    default Type getType() {
        return Type.INTEGER;
    }
}

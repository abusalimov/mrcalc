package com.abusalimov.mrcalc.compile.exprtree;

/**
 * @author Eldar Abusalimov
 */
public interface FloatExpr extends Expr<Double> {
    @Override
    default Type getType() {
        return Type.FLOAT;
    }
}

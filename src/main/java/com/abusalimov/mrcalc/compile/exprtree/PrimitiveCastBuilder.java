package com.abusalimov.mrcalc.compile.exprtree;

/**
 * @author Eldar Abusalimov
 */
public interface PrimitiveCastBuilder<I extends Expr<Long>, F extends Expr<Double>> {
    I toInteger(F expr);

    F toFloat(I expr);
}

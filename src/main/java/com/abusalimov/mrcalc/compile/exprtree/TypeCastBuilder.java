package com.abusalimov.mrcalc.compile.exprtree;

/**
 * @author Eldar Abusalimov
 */
public interface TypeCastBuilder<I extends Expr<Long, I>, F extends Expr<Double, F>> {
    I toInteger(F expr);

    F toFloat(I expr);
}

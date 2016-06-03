package com.abusalimov.mrcalc.compile.exprtree;

/**
 * @author Eldar Abusalimov
 */
public interface BuilderFactory<I extends Expr<Long, I>, F extends Expr<Double, F>> {
    ExprBuilder<Long, I> createIntegerExprBuilder();
    ExprBuilder<Double, F> createFloatExprBuilder();

    TypeCastBuilder<I, F> createTypeCastBuilder();
}

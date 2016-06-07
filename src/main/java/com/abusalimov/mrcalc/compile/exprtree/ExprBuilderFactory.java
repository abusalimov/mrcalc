package com.abusalimov.mrcalc.compile.exprtree;

/**
 * @author Eldar Abusalimov
 */
public interface ExprBuilderFactory<I extends Expr<Long>, F extends Expr<Double>> {
    PrimitiveOpBuilder<Long, I> createIntegerOpBuilder();

    PrimitiveOpBuilder<Double, F> createFloatOpBuilder();

    PrimitiveCastBuilder<I, F> createPrimitiveCastBuilder();
}
package com.abusalimov.mrcalc.compile.impl.function;

import com.abusalimov.mrcalc.compile.exprtree.PrimitiveCastBuilder;

/**
 * @author Eldar Abusalimov
 */
public class FuncPrimitiveCastBuilder implements PrimitiveCastBuilder<IntegerFuncExpr, FloatFuncExpr> {

    @Override
    public IntegerFuncExpr toInteger(FloatFuncExpr expr) {
        return args -> (long) expr.applyAsDouble(args);
    }

    @Override
    public FloatFuncExpr toFloat(IntegerFuncExpr expr) {
        return args -> (double) expr.applyAsLong(args);
    }

}

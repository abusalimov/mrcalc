package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.PrimitiveCastBuilder;

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

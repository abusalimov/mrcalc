package com.abusalimov.mrcalc.compile.impl.function;

import com.abusalimov.mrcalc.compile.exprtree.ExprBuilderFactory;
import com.abusalimov.mrcalc.compile.exprtree.PrimitiveCastBuilder;
import com.abusalimov.mrcalc.compile.exprtree.PrimitiveOpBuilder;

/**
 * @author Eldar Abusalimov
 */
public class FuncExprBuilderFactoryImpl implements ExprBuilderFactory<IntegerFuncExpr, FloatFuncExpr> {
    @Override
    public PrimitiveOpBuilder<Long, IntegerFuncExpr> createIntegerOpBuilder() {
        return new IntegerFuncOpBuilder();
    }

    @Override
    public PrimitiveOpBuilder<Double, FloatFuncExpr> createFloatOpBuilder() {
        return new FloatFuncOpBuilder();
    }

    @Override
    public PrimitiveCastBuilder<IntegerFuncExpr, FloatFuncExpr> createPrimitiveCastBuilder() {
        return new FuncPrimitiveCastBuilder();
    }
}

package com.abusalimov.mrcalc.compile.impl.function;

import com.abusalimov.mrcalc.compile.exprtree.*;

/**
 * @author Eldar Abusalimov
 */
public class FuncExprBuilderFactoryImpl implements ExprBuilderFactory<IntegerFuncExpr, FloatFuncExpr> {
    @Override
    public ObjectOpBuilder<Object, ? extends Expr<Object>, IntegerFuncExpr> createObjectOpBuilder() {
        return new ObjectFuncOpBuilder();
    }

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

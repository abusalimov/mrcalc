package com.abusalimov.mrcalc.compile.impl.function;

import com.abusalimov.mrcalc.compile.exprtree.BuilderFactory;
import com.abusalimov.mrcalc.compile.exprtree.TypeCastBuilder;
import com.abusalimov.mrcalc.compile.exprtree.ExprBuilder;

/**
 * @author Eldar Abusalimov
 */
public class FunctionBuilderFactoryImpl implements BuilderFactory<IntegerFuncExpr, FloatFuncExpr> {
    @Override
    public ExprBuilder<Long, IntegerFuncExpr> createIntegerExprBuilder() {
        return new IntegerFuncExprBuilder();
    }

    @Override
    public ExprBuilder<Double, FloatFuncExpr> createFloatExprBuilder() {
        return new FloatFuncExprBuilder();
    }

    @Override
    public TypeCastBuilder<IntegerFuncExpr, FloatFuncExpr> createTypeCastBuilder() {
        return new FunctionTypeCastBuilder();
    }
}

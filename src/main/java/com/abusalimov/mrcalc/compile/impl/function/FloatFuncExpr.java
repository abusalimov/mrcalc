package com.abusalimov.mrcalc.compile.impl.function;

import com.abusalimov.mrcalc.compile.exprtree.Type;

import java.util.function.ToDoubleFunction;

/**
 * @author Eldar Abusalimov
 */
interface FloatFuncExpr extends FuncExpr<Double>, ToDoubleFunction<Object[]> {
    @Override
    default Type getType() {
        return Type.FLOAT;
    }
}

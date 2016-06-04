package com.abusalimov.mrcalc.compile.impl.function;

import com.abusalimov.mrcalc.compile.exprtree.Type;

import java.util.function.ToLongFunction;

/**
 * @author Eldar Abusalimov
 */
interface IntegerFuncExpr extends FuncExpr<Long>, ToLongFunction<Object[]> {
    @Override
    default Type getType() {
        return Type.INTEGER;
    }
}

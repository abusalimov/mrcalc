package com.abusalimov.mrcalc.compile.impl.function;

import com.abusalimov.mrcalc.compile.exprtree.Expr;

import java.util.function.ToDoubleFunction;

/**
 * @author Eldar Abusalimov
 */
interface FloatFuncExpr extends ToDoubleFunction<Object[]>, Expr<Double> {
}

package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.Expr;

import java.util.function.ToDoubleFunction;

/**
 * @author Eldar Abusalimov
 */
interface FloatFuncExpr extends ToDoubleFunction<Object[]>, Expr<Double> {
}

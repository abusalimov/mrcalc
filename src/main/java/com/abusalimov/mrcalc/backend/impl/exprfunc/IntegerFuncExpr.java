package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.Expr;

import java.util.function.ToLongFunction;

/**
 * @author Eldar Abusalimov
 */
interface IntegerFuncExpr extends ToLongFunction<Object[]>, Expr<Long> {
}

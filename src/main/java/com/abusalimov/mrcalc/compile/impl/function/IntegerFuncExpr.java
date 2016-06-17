package com.abusalimov.mrcalc.compile.impl.function;

import com.abusalimov.mrcalc.compile.exprtree.Expr;

import java.util.function.ToLongFunction;

/**
 * @author Eldar Abusalimov
 */
interface IntegerFuncExpr extends ToLongFunction<Object[]>, Expr<Long> {
}

package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.Expr;

import java.util.function.Function;

/**
 * SAM interface for expression functions.
 *
 * @param <T> the return type of the function (used for more strict type checking)
 * @author Eldar Abusalimov
 */
interface FuncExpr<T> extends Function<Object[], T>, Expr {
}

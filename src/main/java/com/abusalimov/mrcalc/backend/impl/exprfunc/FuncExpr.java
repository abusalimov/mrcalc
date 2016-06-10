package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.Expr;

import java.util.function.Function;

/**
 * @author Eldar Abusalimov
 */
interface FuncExpr<T> extends Function<Object[], T>, Expr {
}

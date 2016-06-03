package com.abusalimov.mrcalc.compile.impl.function;

import com.abusalimov.mrcalc.compile.exprtree.Expr;

/**
 * @author Eldar Abusalimov
 */
interface FuncExpr<T extends Number, E extends FuncExpr<T, E>> extends Expr<T, E> {
}

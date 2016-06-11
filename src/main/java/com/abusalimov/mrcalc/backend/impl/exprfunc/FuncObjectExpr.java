package com.abusalimov.mrcalc.backend.impl.exprfunc;

import java.util.function.Function;

/**
 * @author Eldar Abusalimov
 */
interface FuncObjectExpr<T> extends Function<Object[], T>, FuncExpr<T> {
}

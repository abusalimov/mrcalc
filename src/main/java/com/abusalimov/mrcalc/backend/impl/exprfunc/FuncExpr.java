package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.runtime.Evaluable;

/**
 * SAM interface for expression functions.
 *
 * @param <T> the return type of the function (used for more strict type checking)
 * @author Eldar Abusalimov
 */
interface FuncExpr<T> extends Evaluable<T> {
}

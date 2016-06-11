package com.abusalimov.mrcalc.backend.impl.exprfunc;

import java.util.function.ToLongFunction;

/**
 * @author Eldar Abusalimov
 */
interface LongFuncExpr extends ToLongFunction<Object[]>, FuncExpr<Long> {
}

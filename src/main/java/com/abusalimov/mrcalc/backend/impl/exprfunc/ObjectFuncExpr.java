package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.Expr;

import java.util.function.Function;

/**
 * @author Eldar Abusalimov
 */
// TODO revert to package-local
public interface ObjectFuncExpr extends Function<Object[], Object>, Expr<Object> {
}

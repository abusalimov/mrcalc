package com.abusalimov.mrcalc.compile.impl.function;

import com.abusalimov.mrcalc.compile.exprtree.Expr;

import java.util.function.Function;

/**
 * @author Eldar Abusalimov
 */
// TODO revert to package-local
public interface ObjectFuncExpr extends Function<Object[], Object>, Expr<Object> {
}

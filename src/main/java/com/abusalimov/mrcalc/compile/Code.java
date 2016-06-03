package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.compile.exprtree.Expr;

/**
 * TODO stub implementation
 *
 * @author Eldar Abusalimov
 */
public class Code {
    private final Expr expr;

    public Code(Expr expr) {
        this.expr = expr;
    }

    public Expr getExpr() {
        return expr;
    }
}

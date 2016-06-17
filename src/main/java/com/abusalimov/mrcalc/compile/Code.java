package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.ast.expr.ExprNode;

/**
 * TODO stub implementation
 *
 * @author Eldar Abusalimov
 */
public class Code {
    private final ExprNode exprNode;

    public Code(ExprNode exprNode) {
        this.exprNode = exprNode;
    }

    public ExprNode getExprNode() {
        return exprNode;
    }
}

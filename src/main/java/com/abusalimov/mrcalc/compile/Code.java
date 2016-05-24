package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.ast.ExprNode;

import java.util.Objects;

/**
 * TODO stub implementation
 *
 * @author Eldar Abusalimov
 */
public class Code {
    private final ExprNode exprNode;

    public Code(ExprNode exprNode) {
        this.exprNode = Objects.requireNonNull(exprNode);
    }

    public ExprNode getExprNode() {
        return exprNode;
    }
}

package com.abusalimov.mrcalc.ast.stmt;

import com.abusalimov.mrcalc.ast.NodeVisitor;
import com.abusalimov.mrcalc.ast.expr.ExprNode;

/**
 * The "print" statement evaluates the expression an outputs the result.
 *
 * @author Eldar Abusalimov
 */
public class PrintStmtNode extends AbstractExprStmtNode {
    public PrintStmtNode() {
    }

    public PrintStmtNode(ExprNode expr) {
        super(expr);
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.doVisit(this);
    }
}

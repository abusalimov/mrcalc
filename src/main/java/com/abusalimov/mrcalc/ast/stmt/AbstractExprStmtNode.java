package com.abusalimov.mrcalc.ast.stmt;

import com.abusalimov.mrcalc.ast.AbstractNode;
import com.abusalimov.mrcalc.ast.NodeVisitor;
import com.abusalimov.mrcalc.ast.expr.ExprNode;

import java.util.Collections;
import java.util.List;

/**
 * Thought no statement is technically an "expression" statement, most of them have an intrinsic
 * notion of an expression: variable definitions have an initialization expression, and "print"
 * obviously has an expression to print.
 *
 * @author Eldar Abusalimov
 */
public abstract class AbstractExprStmtNode extends AbstractNode implements StmtNode {
    private ExprNode expr;

    public AbstractExprStmtNode() {
    }

    public AbstractExprStmtNode(ExprNode expr) {
        this.expr = expr;
    }

    public ExprNode getExpr() {
        return expr;
    }

    public void setExpr(ExprNode expr) {
        this.expr = expr;
    }

    @Override
    public List<? extends ExprNode> getChildren() {
        return Collections.singletonList(expr);
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.doVisit(this);
    }
}

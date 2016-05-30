package com.abusalimov.mrcalc.ast.stmt;

import com.abusalimov.mrcalc.ast.AbstractNode;
import com.abusalimov.mrcalc.ast.NodeVisitor;
import com.abusalimov.mrcalc.ast.expr.ExprNode;

import java.util.Collections;
import java.util.List;

/**
 * Expression statement is a special case mainly required by the REPL.
 *
 * @author Eldar Abusalimov
 */
public class ExprStmtNode extends AbstractNode implements StmtNode {
    private ExprNode expr;

    public ExprStmtNode() {
    }

    public ExprStmtNode(ExprNode expr) {
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

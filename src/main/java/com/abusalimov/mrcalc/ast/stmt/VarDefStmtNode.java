package com.abusalimov.mrcalc.ast.stmt;

import com.abusalimov.mrcalc.ast.NodeVisitor;
import com.abusalimov.mrcalc.ast.expr.ExprNode;

/**
 * A variable defines an expression identified using a unique name, there must not be more than one
 * variable with the same name in a single scope.
 *
 * @author Eldar Abusalimov
 */
public class VarDefStmtNode extends AbstractExprStmtNode implements StmtNode {
    private String name;

    public VarDefStmtNode() {
    }

    public VarDefStmtNode(String name, ExprNode expr) {
        super(expr);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.doVisit(this);
    }
}

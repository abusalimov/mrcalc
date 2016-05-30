package com.abusalimov.mrcalc.ast.stmt;

import com.abusalimov.mrcalc.ast.AbstractNode;
import com.abusalimov.mrcalc.ast.NodeVisitor;
import com.abusalimov.mrcalc.ast.expr.ExprNode;

import java.util.Collections;
import java.util.List;

/**
 * A variable defines an expression identified using a unique name, there must not be more than one
 * variable with the same name in a single scope.
 *
 * @author Eldar Abusalimov
 */
public class VarDefStmtNode extends AbstractNode implements StmtNode {
    private String name;
    private ExprNode value;

    public VarDefStmtNode() {
    }

    public VarDefStmtNode(String name, ExprNode value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExprNode getValue() {
        return value;
    }

    public void setValue(ExprNode value) {
        this.value = value;
    }

    @Override
    public List<? extends ExprNode> getChildren() {
        return Collections.singletonList(value);
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.doVisit(this);
    }
}

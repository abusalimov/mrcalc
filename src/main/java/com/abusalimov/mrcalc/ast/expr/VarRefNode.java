package com.abusalimov.mrcalc.ast.expr;

import com.abusalimov.mrcalc.ast.AbstractNode;
import com.abusalimov.mrcalc.ast.NodeArgVisitor;
import com.abusalimov.mrcalc.ast.NodeVisitor;
import com.abusalimov.mrcalc.ast.stmt.VarDefStmtNode;

/**
 * A variable reference must occur after the variable definition in the source code.
 * Compiler links the reference to its definition by the variable name and using scoping rules.
 *
 * @see #getLinkedDef()
 * @author Eldar Abusalimov
 */
public class VarRefNode extends AbstractNode implements ExprNode {
    private String name;

    private VarDefStmtNode linkedDef;

    public VarRefNode() {
    }

    public VarRefNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VarDefStmtNode getLinkedDef() {
        return linkedDef;
    }

    public void setLinkedDef(VarDefStmtNode linkedDef) {
        this.linkedDef = linkedDef;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.doVisit(this);
    }

    @Override
    public <T, A> T accept(NodeArgVisitor<T, A> visitor, A arg) {
        return visitor.doVisit(this, arg);
    }
}

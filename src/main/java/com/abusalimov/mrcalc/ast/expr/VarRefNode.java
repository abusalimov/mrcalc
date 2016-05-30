package com.abusalimov.mrcalc.ast.expr;

import com.abusalimov.mrcalc.ast.AbstractNode;
import com.abusalimov.mrcalc.ast.NodeVisitor;

/**
 * A variable reference must occur after the variable definition in the source code.
 * Compiler links the reference to its definition by the variable name and using scoping rules.
 *
 * @see #getLinkedDef()
 * @author Eldar Abusalimov
 */
public class VarRefNode extends AbstractNode implements ExprNode {
    private String name;

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

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.doVisit(this);
    }
}

package com.abusalimov.mrcalc.ast.expr;

import com.abusalimov.mrcalc.ast.AbstractNode;
import com.abusalimov.mrcalc.ast.NodeVisitor;

/**
 * @author Eldar Abusalimov
 */
public abstract class LiteralNode<V> extends AbstractNode implements ExprNode {
    private V value;

    public LiteralNode() {
    }

    public LiteralNode(V value) {
        this.value = value;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.doVisit(this);
    }
}

package com.abusalimov.mrcalc.ast;

/**
 * @author Eldar Abusalimov
 */
public abstract class LiteralNode<V> implements ExprNode {
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

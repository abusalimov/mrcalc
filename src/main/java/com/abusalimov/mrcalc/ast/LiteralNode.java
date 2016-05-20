package com.abusalimov.mrcalc.ast;

/**
 * @author Eldar Abusalimov
 */
public class LiteralNode<T> implements ExprNode {
    private T value;

    public LiteralNode(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.doVisit(this);
    }
}

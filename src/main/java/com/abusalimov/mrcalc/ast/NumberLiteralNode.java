package com.abusalimov.mrcalc.ast;

/**
 * @author Eldar Abusalimov
 */
public abstract class NumberLiteralNode<V extends Number> extends LiteralNode<V> {

    public NumberLiteralNode() {
    }

    public NumberLiteralNode(V value) {
        super(value);
    }
}

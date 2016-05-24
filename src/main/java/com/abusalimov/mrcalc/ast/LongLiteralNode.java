package com.abusalimov.mrcalc.ast;

/**
 * @author Eldar Abusalimov
 */
public class LongLiteralNode extends NumberLiteralNode<Long> {
    public LongLiteralNode() {
    }

    public LongLiteralNode(Long value) {
        super(value);
    }
}

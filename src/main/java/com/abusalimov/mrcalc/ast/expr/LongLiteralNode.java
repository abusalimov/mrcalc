package com.abusalimov.mrcalc.ast.expr;

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

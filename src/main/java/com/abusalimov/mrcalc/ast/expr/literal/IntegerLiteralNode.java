package com.abusalimov.mrcalc.ast.expr.literal;

import com.abusalimov.mrcalc.ast.NodeArgVisitor;
import com.abusalimov.mrcalc.ast.NodeVisitor;

/**
 * @author Eldar Abusalimov
 */
public class IntegerLiteralNode extends LiteralNode<Long> {
    public IntegerLiteralNode() {
    }

    public IntegerLiteralNode(Long value) {
        super(value);
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

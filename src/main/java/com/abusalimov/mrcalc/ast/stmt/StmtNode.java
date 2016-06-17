package com.abusalimov.mrcalc.ast.stmt;

import com.abusalimov.mrcalc.ast.Node;
import com.abusalimov.mrcalc.ast.NodeArgVisitor;
import com.abusalimov.mrcalc.ast.NodeVisitor;

/**
 * A statement refers to a certain step (action) of the program. Statements don't yield a value.
 *
 * @author Eldar Abusalimov
 */
public interface StmtNode extends Node {
    @Override
    default <T> T accept(NodeVisitor<T> visitor) {
        return visitor.doVisit(this);
    }

    @Override
    default <T, A> T accept(NodeArgVisitor<T, A> visitor, A arg) {
        return visitor.doVisit(this, arg);
    }
}

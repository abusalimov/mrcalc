package com.abusalimov.mrcalc.ast.expr;

import com.abusalimov.mrcalc.ast.Node;
import com.abusalimov.mrcalc.ast.NodeVisitor;

/**
 * Expressions yield some value that can be used within other expressions.
 *
 * @author Eldar Abusalimov
 */
public interface ExprNode extends Node {

    @Override
    default <T> T accept(NodeVisitor<T> visitor) {
        return visitor.doVisit(this);
    }
}

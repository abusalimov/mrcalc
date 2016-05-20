package com.abusalimov.mrcalc.ast;

import java.util.Collections;
import java.util.List;

/**
 * Represents a node of the Abstract Syntax Tree.
 *
 * @author Eldar Abusalimov
 */
public interface Node {
    /**
     * Returns children of the node; used for traversing the AST.
     *
     * @return the list of child nodes, if any
     */
    default List<? extends Node> getChildren() {
        return Collections.emptyList();
    }

    /**
     * Implementations must call an appropriate overloaded visitor method.
     *
     * @param visitor The target of the double-dispatch.
     * @param <T>     The visitor return type.
     * @return The result of calling the visitor method.
     */
    <T> T accept(NodeVisitor<T> visitor);
}

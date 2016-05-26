package com.abusalimov.mrcalc.ast;

import com.abusalimov.mrcalc.location.Location;

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
     * @param visitor the target of the double-dispatch
     * @param <T>     the visitor return type
     * @return the result of calling the visitor method
     */
    <T> T accept(NodeVisitor<T> visitor);

    /**
     * Returns a source location of this node, if any.
     *
     * @return a {@link Location} instance or {@code null}
     */
    Location getLocation();

    /**
     * Attaches location information to this node.
     *
     * @param location a {@link Location} instance or {@code null}
     */
    void setLocation(Location location);
}

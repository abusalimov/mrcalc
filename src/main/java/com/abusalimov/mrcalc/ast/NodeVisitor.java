package com.abusalimov.mrcalc.ast;

/**
 * Basic AST visitor using a double-dispatch pattern. Subclasses of {@link Node} should overload the
 * {@link Node#accept(NodeVisitor)} method to the appropriate {@link #doVisit(Node)} method of the
 * visitor.
 *
 * @param <T> The return type of the {@link #visit(Node)} (and hence the {@link #doVisit(Node)})
 *            operation.
 */
public interface NodeVisitor<T> {

    /**
     * Visit a node, and return a user-defined result of the operation.
     *
     * @param node The subtree to visit.
     * @return The result of visiting the node.
     */
    default T visit(Node node) {
        return node.accept(this);  // double dispatch
    }

    /**
     * Visit the children of a node, and return a user-defined result of the operation.
     *
     * @param node The subtree whose children should be visited.
     */
    default void visitChildren(Node node) {
        node.getChildren().forEach(this::visit);
    }

    default T defaultVisit(Node node) {
        visitChildren(node);
        return null;
    }

    default T doVisit(LiteralNode node) {
        return defaultVisit(node);
    }

    default T doVisit(ExprNode node) {
        return defaultVisit(node);
    }
}

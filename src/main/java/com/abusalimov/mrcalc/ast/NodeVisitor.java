package com.abusalimov.mrcalc.ast;

import com.abusalimov.mrcalc.ast.expr.*;
import com.abusalimov.mrcalc.ast.stmt.ExprStmtNode;
import com.abusalimov.mrcalc.ast.stmt.StmtNode;
import com.abusalimov.mrcalc.ast.stmt.VarDefStmtNode;

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

    default T doVisit(Node node) {
        return defaultVisit(node);
    }

    default T doVisit(ProgramNode node) {
        return doVisit((Node) node);
    }

    default T doVisit(StmtNode node) {
        return doVisit((Node) node);
    }

    default T doVisit(ExprStmtNode node) {
        return doVisit((StmtNode) node);
    }

    default T doVisit(VarDefStmtNode node) {
        return doVisit((StmtNode) node);
    }

    default T doVisit(ExprNode node) {
        return doVisit((Node) node);
    }

    default T doVisit(VarRefNode node) {
        return doVisit((ExprNode) node);
    }

    default T doVisit(LiteralNode node) {
        return doVisit((ExprNode) node);
    }

    default T doVisit(BinaryOpNode node) {
        return doVisit((ExprNode) node);
    }

    default T doVisit(UnaryOpNode node) {
        return doVisit((ExprNode) node);
    }
}
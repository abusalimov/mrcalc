package com.abusalimov.mrcalc.ast;

import com.abusalimov.mrcalc.ast.expr.*;
import com.abusalimov.mrcalc.ast.expr.literal.FloatLiteralNode;
import com.abusalimov.mrcalc.ast.expr.literal.IntegerLiteralNode;
import com.abusalimov.mrcalc.ast.expr.literal.LiteralNode;
import com.abusalimov.mrcalc.ast.stmt.OutStmtNode;
import com.abusalimov.mrcalc.ast.stmt.PrintStmtNode;
import com.abusalimov.mrcalc.ast.stmt.StmtNode;
import com.abusalimov.mrcalc.ast.stmt.VarDefStmtNode;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Basic AST visitor using a double-dispatch pattern with a single visitor argument.
 *
 * @param <T> The return type of the {@link #visit(Node, Object)} (and hence the {@link
 *            #doVisit(Node, Object)}) operation.
 * @param <A> The type of the argument passed to the {@link #visit(Node, Object)} method and the
 *            visitor methods.
 * @author Eldar Abusalimov
 * @see NodeVisitor visitor with no arguments
 */
public interface NodeArgVisitor<T, A> {

    /**
     * Visit a node, and return a user-defined result of the operation.
     *
     * @param node The subtree to visit
     * @param arg  The argument to pass through the visitor methods
     * @return The result of visiting the node
     */
    default T visit(Node node, A arg) {
        return node.accept(this, arg);  // double dispatch
    }

    /**
     * Visit the children of a node.
     *
     * @param node The subtree whose children should be visited
     * @param arg  The argument to pass through the visitor methods
     */
    default void visitChildren(Node node, A arg) {
        node.getChildren().forEach(child -> visit(child, arg));
    }

    /**
     * Visit the children of a node, and return a user-defined result of the operation.
     *
     * @param node The subtree whose children should be visited
     * @param arg  The argument to pass through the visitor methods
     * @return A list of values returned by {@link #visit(Node, Object)} invoked on each child
     */
    default List<? extends T> visitChildrenWithResult(Node node, A arg) {
        return node.getChildren().stream()
                .map(child -> visit(child, arg))
                .collect(Collectors.toList());
    }

    default T defaultVisit(Node node, A arg) {
        visitChildren(node, arg);
        return null;
    }

    default T doVisit(Node node, A arg) {
        return defaultVisit(node, arg);
    }

    default T doVisit(ProgramNode node, A arg) {
        return doVisit((Node) node, arg);
    }

    default T doVisit(StmtNode node, A arg) {
        return doVisit((Node) node, arg);
    }

    default T doVisit(VarDefStmtNode node, A arg) {
        return doVisit((StmtNode) node, arg);
    }

    default T doVisit(PrintStmtNode node, A arg) {
        return doVisit((StmtNode) node, arg);
    }

    default T doVisit(OutStmtNode node, A arg) {
        return doVisit((StmtNode) node, arg);
    }

    default T doVisit(ExprNode node, A arg) {
        return doVisit((Node) node, arg);
    }

    default T doVisit(VarRefNode node, A arg) {
        return doVisit((ExprNode) node, arg);
    }

    default T doVisit(BinaryOpNode node, A arg) {
        return doVisit((ExprNode) node, arg);
    }

    default T doVisit(UnaryOpNode node, A arg) {
        return doVisit((ExprNode) node, arg);
    }

    default T doVisit(LiteralNode<?> node, A arg) {
        return doVisit((ExprNode) node, arg);
    }

    default T doVisit(IntegerLiteralNode node, A arg) {
        return doVisit((LiteralNode<?>) node, arg);
    }

    default T doVisit(FloatLiteralNode node, A arg) {
        return doVisit((LiteralNode<?>) node, arg);
    }

    default T doVisit(RangeNode node, A arg) {
        return doVisit((ExprNode) node, arg);
    }

    default T doVisit(MapNode node, A arg) {
        return doVisit((ExprNode) node, arg);
    }

    default T doVisit(ReduceNode node, A arg) {
        return doVisit((ExprNode) node, arg);
    }

    default T doVisit(LambdaNode node, A arg) {
        return doVisit((Node) node, arg);
    }
}

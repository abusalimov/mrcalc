package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.ast.ExprHolderNode;
import com.abusalimov.mrcalc.ast.expr.ExprNode;
import com.abusalimov.mrcalc.compile.type.Type;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Encapsulates the results of type inference by mapping each sub-expression to its type.
 * <p>
 * Each {@link ExprTypeInfo} instance maintains a type mapping for an AST subtree rooted by a given {@link
 * ExprHolderNode} and pruned at descendant {@code ExprHolderNode}s, if any. In other words, a single instance is only
 * responsible for {@link ExprNode sub-expressions} that have the {@code ExprHolderNode} associated with the {@code
 * ExprTypeInfo} instance as the closest ascendant node of that type in the AST. The rest subtrees (pruned at their
 * {@code ExprHolderNode}s), in turn, are handled by other {@code ExprTypeInfo} instances that are treated as {@link
 * #getChild(ExprHolderNode) children} of the containing {@code ExprTypeInfo} instance.
 * <p>
 * The rationale behind this design decision is that the {@link TypeInferrer} relies on a variables scope containing
 * certain sub-expression, and that scope happens to be associated with the closest ExprHolderNode holding that
 * sub-expression.
 * <p>
 * For example, given a simple expression statement:
 * <pre><code>
 *     print map(seq, e -> e^2)
 * </code></pre>
 *
 * The type inferrer creates two ExprTypeInfo instances:
 * <ol>
 * <li> one for the "print" statement (the topmost one), maintaining the {@code map(...)} and {@code seq}
 *      sub-expressions;
 *
 * <li> and one for the {@code e -> ...} lambda function as a child of the topmost instance, responsible for the
 *      {@code e^2}, {@code e}, and {@code 2} sub-expressions.
 * </ol>
 *
 * @author Eldar Abusalimov
 */
public class ExprTypeInfo {
    private final ExprHolderNode holderNode;
    private final Map<String, Variable> variableMap;

    private final Map<ExprNode, Type> exprTypeMap = new HashMap<>();
    private final Map<ExprHolderNode, ExprTypeInfo> childMap = new LinkedHashMap<>();

    /**
     * Creates a new instance for the given {@link ExprHolderNode} and variables mapping.
     *
     * @param holderNode  the root for an AST subtree for sub-expressions of which to hold the type info
     * @param variableMap the mapping of variables that can be {@link #getVariable(String) queried} within the
     *                    expression
     */
    public ExprTypeInfo(ExprHolderNode holderNode, Map<String, Variable> variableMap) {
        this.holderNode = Objects.requireNonNull(holderNode);
        this.variableMap = Objects.requireNonNull(variableMap);
    }

    /**
     * Retrieves a {@link Variable} defined in the associated variables scope, if any, by its name.
     *
     * @param name the name of the variable to get
     * @return the variable, if any, null otherwise
     */
    public Variable getVariable(String name) {
        return variableMap.get(name);
    }


    /**
     * Returns the {@link ExprHolderNode} provided at the {@link #ExprTypeInfo(ExprHolderNode, Map) creation} time.
     *
     * @return the non-null {@code ExprHolderNode} instance
     */
    public ExprHolderNode getExprHolderNode() {
        return holderNode;
    }

    /**
     * Returns the immediate {@link ExprNode expression} held by the {@link #getExprHolderNode() ExprHolderNode}.
     *
     * @return the root expression
     */
    public ExprNode getExprNode() {
        return holderNode.getExpr();
    }

    /**
     * Used by the {@link TypeInferrer} to record a {@link Type} for a given {@link ExprNode sub-expression}.
     *
     * @param exprNode the sub-expression for which to save the type
     * @param type     the inferred type of the sub-expression
     */
    public void putExprType(ExprNode exprNode, Type type) {
        exprTypeMap.put(exprNode, Objects.requireNonNull(type));
    }

    /**
     * Returns the inferred type of the {@link #getExprNode() root expression}. It is an error to call this method until
     * the type inference is finished.
     *
     * @return the non-null {@link Type} of the expression held by the {@link #getExprHolderNode() ExprHolderNode}
     * @throws IllegalStateException if the type has not been inferred yet
     */
    public Type getExprType() {
        return getExprType(getExprNode());
    }

    /**
     * Returns the inferred type of the given {@link ExprNode expression}. It is an error to call this method until
     * the type inference is finished or for expressions not contained by the {@link #getExprHolderNode() holder node}.
     *
     * @return the non-null {@link Type} of the given expression
     * @throws IllegalStateException if the type of the specified expression has not been inferred yet, or this
     *                               {@code ExprTypeInfo} instance is not responsible for the given expression at all
     */
    public Type getExprType(ExprNode exprNode) {
        return exprTypeMap.computeIfAbsent(exprNode, node -> {
            throw new IllegalStateException("No inferred type for expr " + node);
        });
    }

    /**
     * Records the specified {@code ExprTypeInfo} as a new child. It can be retrieved back using the {@link
     * #getChild(ExprHolderNode)} method later on.
     *
     * @param exprTypeInfo the instance to save as the child
     */
    public void addChild(ExprTypeInfo exprTypeInfo) {
        childMap.put(exprTypeInfo.getExprHolderNode(), Objects.requireNonNull(exprTypeInfo));
    }

    /**
     * Retrieves an immediate child {@link ExprTypeInfo} instance by its {@link #getExprHolderNode() holder node}.
     *
     * @param holderNode the holder node of the child to get
     * @return a child instance, if any
     */
    public ExprTypeInfo getChild(ExprHolderNode holderNode) {
        return childMap.get(holderNode);
    }
}
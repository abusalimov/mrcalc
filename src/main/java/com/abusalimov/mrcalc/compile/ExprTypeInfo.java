package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.ast.ExprHolderNode;
import com.abusalimov.mrcalc.ast.expr.ExprNode;
import com.abusalimov.mrcalc.compile.type.Type;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Eldar Abusalimov
 */
public class ExprTypeInfo {
    private final ExprHolderNode holderNode;
    private final Map<String, Variable> variableMap;

    private final Map<ExprNode, Type> exprTypeMap = new HashMap<>();
    private final Map<ExprHolderNode, ExprTypeInfo> childMap = new LinkedHashMap<>();

    public ExprTypeInfo(ExprHolderNode holderNode, Map<String, Variable> variableMap) {
        this.holderNode = holderNode;
        this.variableMap = variableMap;
    }

    public Variable getVariable(String name) {
        return variableMap.get(name);
    }

    public ExprHolderNode getExprHolderNode() {
        return holderNode;
    }

    public ExprNode getExprNode() {
        return holderNode.getExpr();
    }

    public void putExprType(ExprNode exprNode, Type type) {
        exprTypeMap.put(exprNode, Objects.requireNonNull(type));
    }

    public Type getExprType() {
        return getExprType(getExprNode());
    }

    public Type getExprType(ExprNode exprNode) {
        return exprTypeMap.computeIfAbsent(exprNode, node -> {
            throw new IllegalStateException("No inferred type for expr");
        });
    }

    public Map<ExprNode, Type> getExprTypeMap() {
        return exprTypeMap;
    }

    public void addChild(ExprTypeInfo exprTypeInfo) {
        childMap.put(exprTypeInfo.getExprHolderNode(), Objects.requireNonNull(exprTypeInfo));
    }

    public ExprTypeInfo getChild(ExprHolderNode holderNode) {
        return childMap.get(holderNode);
    }

    public Map<ExprHolderNode, ExprTypeInfo> getChildMap() {
        return childMap;
    }
}

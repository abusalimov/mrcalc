package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.ast.ExprHolderNode;
import com.abusalimov.mrcalc.ast.expr.ExprNode;
import com.abusalimov.mrcalc.compile.type.Primitive;
import com.abusalimov.mrcalc.compile.type.Type;

import java.util.*;

/**
 * @author Eldar Abusalimov
 */
public class ExprTypeInfo {
    private final ExprHolderNode holderNode;
    private final Map<String, Variable> variableMap;

    private final Map<ExprNode, Type> exprTypeMap = new HashMap<>();
    private final Map<ExprHolderNode, ExprTypeInfo> childMap = new LinkedHashMap<>();
    private final Map<Variable, Integer> referencedVariableMap = new LinkedHashMap<>();

    public ExprTypeInfo(ExprHolderNode holderNode, Map<String, Variable> variableMap) {
        this.holderNode = holderNode;
        this.variableMap = variableMap;
    }

    public boolean isComplete() {
        return (!exprTypeMap.isEmpty() &&
                exprTypeMap.values().stream().allMatch(type -> type.getPrimitive() != Primitive.UNKNOWN) &&
                childMap.values().stream().allMatch(ExprTypeInfo::isComplete));
    }

    public Variable getVariable(String name) {
        return variableMap.get(name);
    }

    public Variable referenceVariable(String name) {
        return variableMap.computeIfPresent(name, (s, variable) -> {
            referencedVariableMap.put(variable, referencedVariableMap.size());
            return variable;
        });
    }

    public int getReferencedVariableIndex(String name) {
        Variable variable = variableMap.get(name);
        return referencedVariableMap.getOrDefault(variable, -1);
    }

    public List<Variable> getReferencedVariables() {
        return new ArrayList<>(referencedVariableMap.keySet());
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
            throw new IllegalStateException("No inferred type for expr " + node);
        });
    }

    public Map.Entry<ExprNode, Type> entry(ExprNode exprNode) {
        return new AbstractMap.SimpleImmutableEntry<>(exprNode, getExprType(exprNode));
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

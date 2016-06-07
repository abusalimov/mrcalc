package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.ast.LambdaNode;
import com.abusalimov.mrcalc.ast.Node;
import com.abusalimov.mrcalc.ast.NodeVisitor;
import com.abusalimov.mrcalc.ast.expr.*;
import com.abusalimov.mrcalc.ast.expr.literal.FloatLiteralNode;
import com.abusalimov.mrcalc.ast.expr.literal.IntegerLiteralNode;
import com.abusalimov.mrcalc.compile.type.Primitive;
import com.abusalimov.mrcalc.compile.type.Sequence;
import com.abusalimov.mrcalc.compile.type.Type;
import com.abusalimov.mrcalc.diagnostic.Diagnostic;

import java.util.Map;

/**
 * @author Eldar Abusalimov
 */
public class TypeInferrer extends AbstractNodeDiagnosticEmitter implements NodeVisitor<Type> {

    private final Map<String, Variable> variableMap;

    public TypeInferrer(Map<String, Variable> variableMap) {
        this.variableMap = variableMap;
    }

    public Map<String, Variable> getVariableMap() {
        return variableMap;
    }

    @Override
    public Type doVisit(VarRefNode node) {
        Variable variable = variableMap.get(node.getName());

        if (variable == null) {
            emitNodeDiagnostic(node,
                    String.format("Undefined variable '%s'", node.getName()));
            return Primitive.UNKNOWN;
        }

        return variable.getType();
    }

    @Override
    public Type doVisit(IntegerLiteralNode node) {
        return Primitive.INTEGER;
    }

    @Override
    public Type doVisit(FloatLiteralNode node) {
        return Primitive.FLOAT;
    }

    @Override
    public Type doVisit(BinaryOpNode node) {
        Type leftType = visit(node.getOperandA());
        Type rightType = visit(node.getOperandB());

        if (!(leftType instanceof Primitive && rightType instanceof Primitive)) {
            emitDiagnostic(new Diagnostic(node.getLocation(),
                    String.format("Operator '%s' cannot be applied to '%s' and '%s'",
                            node.getOp().getSign(), leftType, rightType)));
            return Primitive.UNKNOWN;
        }

        return Primitive.promote((Primitive) leftType, (Primitive) rightType);
    }

    @Override
    public Type doVisit(UnaryOpNode node) {
        return visit(node.getOperand());
    }

    @Override
    public Type doVisit(RangeNode node) {
        Type elementType = Primitive.INTEGER;

        for (Node child : node.getChildren()) {
            Type childType = visit(child);
            if (childType != Primitive.INTEGER) {
                /* Avoid cascade reporting in case of inner expression errors. */
                if (childType != Primitive.UNKNOWN) {
                    emitNodeDiagnostic(child,
                            String.format("Range cannot have '%s' as its boundary",
                                    childType));
                }
                elementType = Primitive.UNKNOWN;
            }
        }

        return Sequence.of(elementType);
    }

    @Override
    public Type doVisit(MapNode node) {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public Type doVisit(ReduceNode node) {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public Type doVisit(LambdaNode node) {
        throw new UnsupportedOperationException("NIY");
    }
}

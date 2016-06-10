package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.ast.ExprHolderNode;
import com.abusalimov.mrcalc.ast.LambdaNode;
import com.abusalimov.mrcalc.ast.Node;
import com.abusalimov.mrcalc.ast.NodeArgVisitor;
import com.abusalimov.mrcalc.ast.expr.*;
import com.abusalimov.mrcalc.ast.expr.literal.FloatLiteralNode;
import com.abusalimov.mrcalc.ast.expr.literal.IntegerLiteralNode;
import com.abusalimov.mrcalc.compile.type.Primitive;
import com.abusalimov.mrcalc.compile.type.Sequence;
import com.abusalimov.mrcalc.compile.type.Type;
import com.abusalimov.mrcalc.diagnostic.DiagnosticListener;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Type inferrer deduces types of expressions based on their sub-expressions or surrounding constructions.
 * <p>
 * The type inferrer is also responsible for reporting any type errors found in expressions, like attempting to map a
 * scalar value or to add two sequences together.
 * <p>
 * Type inference is performed on AST nodes {@link ExprHolderNode holding an expression}, i.e. most of
 * {@link com.abusalimov.mrcalc.ast.stmt.StmtNode statement} nodes and {@link LambdaNode lambdas}. The type inference
 * results are encapsulated in a {@link ExprTypeInfo} instance that maps each sub-expression to its inferred type.
 * Instances of {@link TypeInferrer} are stateless, and the necessary state is passed on as a visitor argument within
 * the {@link ExprTypeInfo} instance being constructed.
 *
 * @author Eldar Abusalimov
 */
public class TypeInferrer extends AbstractNodeDiagnosticEmitter implements NodeArgVisitor<Type, ExprTypeInfo> {

    /**
     * Given an AST node {@link ExprHolderNode holding an expression} and the global variables mapping, creates a new
     * {@link ExprTypeInfo} instance and fills it in by inferring types of all the sub-expressions.
     *
     * @param holderNode  the AST node to infer types for
     * @param variableMap the global variables mapping
     * @return the newly created and populated {@link ExprTypeInfo} instance, {@link ExprTypeInfo#isComplete()
     * incomplete} in case of type errors
     */
    public ExprTypeInfo infer(ExprHolderNode holderNode, Map<String, Variable> variableMap) {
        ExprTypeInfo exprTypeInfo = new ExprTypeInfo(holderNode, variableMap);
        inferType(exprTypeInfo);
        return exprTypeInfo;
    }

    /**
     * The same as {@link #infer(ExprHolderNode, Map)}, but also reports diagnostic errors discovered during the
     * process, if any, to the specified {@link DiagnosticListener}.
     *
     * @param holderNode         the AST node to infer types for
     * @param variableMap        the global variables mapping
     * @param diagnosticListener the listener to report type errors to
     * @return the {@link ExprTypeInfo} instance
     * @see #infer(ExprHolderNode, Map)
     */
    public ExprTypeInfo infer(ExprHolderNode holderNode, Map<String, Variable> variableMap,
                              DiagnosticListener diagnosticListener) {
        return runWithDiagnosticListener(() -> infer(holderNode, variableMap), diagnosticListener);
    }

    protected ExprTypeInfo inferChild(ExprTypeInfo parentExprTypeInfo,
                                      ExprHolderNode holderNode, Map<String, Variable> variableMap) {
        ExprTypeInfo exprTypeInfo = infer(holderNode, variableMap);
        parentExprTypeInfo.addChild(exprTypeInfo);
        return exprTypeInfo;
    }

    protected ExprTypeInfo inferLambda(ExprTypeInfo parentExprTypeInfo, LambdaNode lambda, Type... argTypes) {
        Map<String, Variable> argVariableMap = createArgMap(lambda.getArgNames(), argTypes);
        return inferChild(parentExprTypeInfo, lambda, argVariableMap);
    }

    protected Type inferType(ExprTypeInfo exprTypeInfo) {
        Type type = visit(exprTypeInfo.getExprNode(), exprTypeInfo);
        assert exprTypeInfo.getExprType() == type;
        return type;
    }

    @Override
    public Type visit(Node node, ExprTypeInfo exprTypeInfo) {
        Type type = NodeArgVisitor.super.visit(node, exprTypeInfo);
        exprTypeInfo.putExprType((ExprNode) node, type);
        return type;
    }

    @Override
    public Type doVisit(Node node, ExprTypeInfo exprTypeInfo) {
        throw new UnsupportedOperationException("Expressions only");
    }

    @Override
    public Type doVisit(VarRefNode node, ExprTypeInfo exprTypeInfo) {
        Variable variable = exprTypeInfo.referenceVariable(node.getName());

        if (variable == null) {
            emitNodeDiagnostic(node,
                    String.format("Undefined variable '%s'", node.getName()));
            return Primitive.UNKNOWN;
        }

        return variable.getType();
    }

    @Override
    public Type doVisit(IntegerLiteralNode node, ExprTypeInfo exprTypeInfo) {
        return Primitive.INTEGER;
    }

    @Override
    public Type doVisit(FloatLiteralNode node, ExprTypeInfo exprTypeInfo) {
        return Primitive.FLOAT;
    }

    @Override
    public Type doVisit(BinaryOpNode node, ExprTypeInfo exprTypeInfo) {
        Type leftType = visit(node.getOperandA(), exprTypeInfo);
        Type rightType = visit(node.getOperandB(), exprTypeInfo);

        if (!(leftType instanceof Primitive && rightType instanceof Primitive)) {
            emitNodeDiagnostic(node,
                    String.format("Operator '%s' cannot be applied to '%s' and '%s'",
                            node.getOp().getSign(), leftType, rightType));
            return Primitive.UNKNOWN;
        }

        return Primitive.promote((Primitive) leftType, (Primitive) rightType);
    }

    @Override
    public Type doVisit(UnaryOpNode node, ExprTypeInfo exprTypeInfo) {
        return visit(node.getOperand(), exprTypeInfo);
    }

    @Override
    public Type doVisit(RangeNode node, ExprTypeInfo exprTypeInfo) {
        Type elementType = Primitive.INTEGER;

        for (Node child : node.getChildren()) {
            Type childType = visit(child, exprTypeInfo);
            if (childType != Primitive.INTEGER) {
                /* Avoid cascade reporting in case of inner expression errors. */
                if (childType != Primitive.UNKNOWN) {
                    emitNodeDiagnostic(child,
                            String.format("Range cannot have '%s' as its boundary", childType));
                }
                elementType = Primitive.UNKNOWN;
            }
        }

        return Sequence.of(elementType);
    }

    @Override
    public Type doVisit(MapNode node, ExprTypeInfo exprTypeInfo) {
        ExprNode sequence = node.getSequence();
        Type sequenceElementType = checkSequenceType(visit(sequence, exprTypeInfo), sequence, "map()");

        LambdaNode lambda = node.getLambda();

        if (!checkLambdaArity(lambda, 1, "map()")) {
            return Sequence.of(Primitive.UNKNOWN);
        }

        return Sequence.of(inferLambda(exprTypeInfo, lambda, sequenceElementType).getExprType());
    }

    @Override
    public Type doVisit(ReduceNode node, ExprTypeInfo exprTypeInfo) {
        ExprNode sequence = node.getSequence();
        Type sequenceElementType = checkSequenceType(visit(sequence, exprTypeInfo), sequence, "reduce()");
        Type neutralType = visit(node.getNeutral(), exprTypeInfo);

        LambdaNode lambda = node.getLambda();

        if (!checkLambdaArity(lambda, 2, "reduce()")) {
            return Primitive.UNKNOWN;
        }

        Type lambdaType = inferLambda(exprTypeInfo, lambda, neutralType, sequenceElementType).getExprType();
        if (!neutralType.equals(lambdaType)) {
            if (neutralType != Primitive.UNKNOWN && lambdaType != Primitive.UNKNOWN) {
                emitNodeDiagnostic(lambda,
                        String.format("Lambda return type '%s' is incompatible with neutral element type '%s'",
                                lambdaType, neutralType));
            }
            return Primitive.UNKNOWN;
        }

        return lambdaType;
    }

    private Map<String, Variable> createArgMap(List<String> argNames, Type... argTypes) {
        if (argNames.size() != argTypes.length) {
            throw new IllegalArgumentException();
        }
        Map<String, Variable> argMap = new LinkedHashMap<>();
        for (String argName : argNames) {
            argMap.put(argName, new Variable(argName, argTypes[argMap.size()]));
        }
        // TODO ensure no duplicate variables
        return argMap;
    }

    private boolean checkLambdaArity(LambdaNode lambda, int arity, String funcName) {
        boolean valid = (lambda.getArgNames().size() == arity);
        if (!valid) {
            emitNodeDiagnostic(lambda,
                    String.format("Lambda for %s accepts exactly %d argument%s", funcName, arity,
                            arity == 1 ? "" : "s"));
        }
        return valid;
    }

    private Type checkSequenceType(Type type, ExprNode sequence, String funcName) {
        if (!(type instanceof Sequence)) {
            if (type != Primitive.UNKNOWN) {
                emitNodeDiagnostic(sequence,
                        String.format("%s cannot be applied to a scalar '%s'", funcName, type));
            }
            return Primitive.UNKNOWN;
        }
        return ((Sequence) type).getElementType();
    }
}

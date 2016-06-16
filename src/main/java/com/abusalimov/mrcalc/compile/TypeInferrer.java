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

import java.util.*;

/**
 * Type inferrer deduces types of expressions based on their sub-expressions or surrounding constructions.
 * <p>
 * Rules that are used to infer a type of an expression are simple and straightforward:
 * <ul>
 * <li> Literal constants are {@link Primitive} scalars: either {@link Primitive#INTEGER} or {@link Primitive#FLOAT};
 *
 * <li> References to a global variable obviously yield a type of that variable. The type inferrer relies on an
 *      externally provided mapping of typed variables;
 *
 * <li> Unary operation expression has a primitive type of the sole operand;
 *
 * <li> Binary operation expression type is inferred from the operands using {@link Primitive#promote(List) promotion}
 *      rules by widening the operand types to a common primitive type. For example, adding an {@link Primitive#INTEGER}
 *      and a {@link Primitive#FLOAT} together yields a {@link Primitive#FLOAT} as the most wide types of two;
 *
 * <li> Ranges yield a {@link Sequence} of {@link Primitive#INTEGER}s;
 *
 * <li> A call to the map() function yields a {@link Sequence} of a return type of its lambda applied to an element of a
 *      sequence passed in: {@code map([T], (T -> R)) -> [R]};
 *
 * <li> A call to the reduce() function yields a type of its neutral element, which, in turn, must be also exactly the
 *      same as a return type of a lambda applied to that neutral element and an element of a sequence passed in:
 *      {@code reduce([T], R, (R, T -> R)) -> R};
 *
 * <li> The return type of a lambda used by map() and reduce() functions is determined by following the inference rules
 *      applied within a variable context of the lambda arguments, with type of these arguments provided by the
 *      surrounding map/reduce function.
 * </ul>
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

    protected Type inferLambdaType(ExprTypeInfo parentExprTypeInfo, String funcName, LambdaNode lambda,
                                   Type... argTypes) {
        if (!checkLambdaArity(lambda, argTypes.length, funcName)) {
            return Primitive.UNKNOWN;
        }
        List<Variable> argVariableMap = createArgList(lambda.getArgNames(), argTypes);

        ExprTypeInfo exprTypeInfo = new LambdaExprTypeInfo(lambda, argVariableMap);
        inferType(exprTypeInfo);
        parentExprTypeInfo.addChild(exprTypeInfo);

        return exprTypeInfo.getExprType();
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

        Type lambdaType = inferLambdaType(exprTypeInfo, "map()", node.getLambda(), sequenceElementType);

        return Sequence.of(lambdaType);
    }

    @Override
    public Type doVisit(ReduceNode node, ExprTypeInfo exprTypeInfo) {
        ExprNode sequence = node.getSequence();
        Type sequenceElementType = checkSequenceType(visit(sequence, exprTypeInfo), sequence, "reduce()");
        Type neutralType = visit(node.getNeutral(), exprTypeInfo);

        Type lambdaType = inferLambdaType(exprTypeInfo, "reduce()", node.getLambda(), neutralType, sequenceElementType);

        if (!neutralType.equals(lambdaType)) {
            if (neutralType != Primitive.UNKNOWN && lambdaType != Primitive.UNKNOWN) {
                emitNodeDiagnostic(node.getLambda(),
                        String.format("Lambda return type '%s' is incompatible with neutral element type '%s'",
                                lambdaType, neutralType));
            }
            return Primitive.UNKNOWN;
        }

        return lambdaType;
    }

    private boolean checkLambdaArity(LambdaNode lambda, int arity, String funcName) {
        List<String> argNames = lambda.getArgNames();
        if (argNames.size() != arity) {
            emitNodeDiagnostic(lambda,
                    String.format("Lambda for %s accepts exactly %d argument%s", funcName, arity,
                            arity == 1 ? "" : "s"));
        }

        Set<String> seenArgNames = new HashSet<>();
        Set<String> duplicateArgNames = new LinkedHashSet<>();
        for (String argName : argNames) {
            if (!seenArgNames.add(argName)) {
                duplicateArgNames.add(argName);
            }
        }

        /* Report each duplicate name only once, even if some of them occur three times or more. */
        for (String argName : duplicateArgNames) {
            emitNodeDiagnostic(lambda,
                    String.format("Duplicate lambda argument '%s'", argName));
        }

        return (argNames.size() == arity) && duplicateArgNames.isEmpty();
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

    private List<Variable> createArgList(List<String> argNames, Type... argTypes) {
        if (argNames.size() != argTypes.length) {
            throw new IllegalArgumentException("Lambda arity mismatch");
        }

        List<Variable> args = new ArrayList<>(argNames.size());
        for (String argName : argNames) {
            args.add(new Variable(argName, argTypes[args.size()]));
        }

        return args;
    }

    /**
     * Provides special indexing of variables referring to lambda arguments.
     */
    protected static class LambdaExprTypeInfo extends ExprTypeInfo {

        private final Map<Variable, Integer> argIndexMap = new LinkedHashMap<>();

        /**
         * Creates a new instance for the given {@link ExprHolderNode} and variables mapping.
         *
         * @param lambda the lambda for sub-expressions of which to hold the type info
         * @param args   the list of argument variables
         * @throws IllegalArgumentException if some of the arguments have duplicate name
         */
        public LambdaExprTypeInfo(LambdaNode lambda, List<Variable> args) {
            super(lambda, createArgMap(args));

            for (Variable arg : args) {
                argIndexMap.put(arg, argIndexMap.size());
            }
        }

        private static Map<String, Variable> createArgMap(List<Variable> args) {
            Map<String, Variable> argMap = new LinkedHashMap<>();
            for (Variable arg : args) {
                argMap.put(arg.getName(), arg);
            }

            if (argMap.size() != args.size()) {
                throw new IllegalArgumentException("Duplicate lambda argument names");
            }
            return argMap;
        }

        @Override
        public int getReferencedVariableIndex(String name) {
            Variable variable = getVariable(name);
            return argIndexMap.getOrDefault(variable, -1);
        }
    }
}

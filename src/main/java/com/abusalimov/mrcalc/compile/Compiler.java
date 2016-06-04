package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.ast.ExprHolderNode;
import com.abusalimov.mrcalc.ast.Node;
import com.abusalimov.mrcalc.ast.NodeVisitor;
import com.abusalimov.mrcalc.ast.ProgramNode;
import com.abusalimov.mrcalc.ast.expr.BinaryOpNode;
import com.abusalimov.mrcalc.ast.expr.ExprNode;
import com.abusalimov.mrcalc.ast.expr.UnaryOpNode;
import com.abusalimov.mrcalc.ast.expr.VarRefNode;
import com.abusalimov.mrcalc.ast.expr.literal.FloatLiteralNode;
import com.abusalimov.mrcalc.ast.expr.literal.IntegerLiteralNode;
import com.abusalimov.mrcalc.ast.stmt.StmtNode;
import com.abusalimov.mrcalc.ast.stmt.VarDefStmtNode;
import com.abusalimov.mrcalc.compile.exprtree.*;
import com.abusalimov.mrcalc.compile.impl.function.FuncExprBuilderFactoryImpl;
import com.abusalimov.mrcalc.diagnostic.AbstractDiagnosticEmitter;
import com.abusalimov.mrcalc.diagnostic.Diagnostic;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Eldar Abusalimov
 */
public class Compiler extends AbstractDiagnosticEmitter {

    private Map<String, VarDefStmtNode> varDefMap = new HashMap<>();
    private Map<Node, Type> typeMap = new HashMap<>();

    private ExprBuilderFactory<?, ?> exprBuilderFactory;

    public Compiler() {
        this(new FuncExprBuilderFactoryImpl());
    }

    public Compiler(ExprBuilderFactory<?, ?> exprBuilderFactory) {
        this.exprBuilderFactory = exprBuilderFactory;
    }

    public Code compile(ProgramNode node) throws CompileErrorException {
        try (ListenerClosable<CompileErrorException> ignored =
                     collectDiagnosticsToThrow(CompileErrorException::new)) {
            collectVariables(node);
            inferTypes(node);

            List<StmtNode> stmts = node.getStmts();

            ExprNode exprNode = null;
            if (!stmts.isEmpty()) {
                StmtNode lastStmt = stmts.get(stmts.size() - 1);
                if (lastStmt instanceof ExprHolderNode) {
                    exprNode = ((ExprHolderNode) lastStmt).getExpr();
                }
            }

            if (exprNode != null) {
                return compileExpr(exprNode);
            } else {
                return new Code(null);
            }
        }
    }

    public Code compileExpr(ExprNode node) throws CompileErrorException {
        return new Code(buildExpr(node));
    }

    protected void collectVariables(ProgramNode rootNode) {
        new NodeVisitor<Void>() {
            @Override
            public Void doVisit(VarDefStmtNode node) {
                /*
                 * Need to visit the value prior to defining a variable in the scope in order
                 * to forbid self-recursive variable references from within the definition:
                 *
                 *   var r = r  # error
                 */
                visit(node.getExpr());

                if (varDefMap.putIfAbsent(node.getName(), node) != null) {
                    emitDiagnostic(new Diagnostic(node.getLocation(),
                            String.format("Variable '%s' is already defined", node.getName())));
                }
                return null;
            }

            @Override
            public Void doVisit(VarRefNode node) {
                VarDefStmtNode varDef = varDefMap.get(node.getName());
                if (varDef != null) {
                    node.setLinkedDef(varDef);
                } else {
                    emitDiagnostic(new Diagnostic(node.getLocation(),
                            String.format("Undefined variable '%s'", node.getName())));
                }
                return null;
            }
        }.visit(rootNode);
    }

    protected void inferTypes(ProgramNode rootNode) {
        new NodeVisitor<Type>() {
            @Override
            public Type visit(Node node) {
                Type type = NodeVisitor.super.visit(node);
                typeMap.put(node, type);
                return type;
            }

            @Override
            public Type doVisit(VarRefNode node) {
                VarDefStmtNode linkedDef = node.getLinkedDef();
                if (linkedDef != null) {
                    return getNodeType(linkedDef.getExpr());
                }
                return Type.UNKNOWN;
            }

            @Override
            public Type doVisit(IntegerLiteralNode node) {
                return Type.INTEGER;
            }

            @Override
            public Type doVisit(FloatLiteralNode node) {
                return Type.FLOAT;
            }

            @Override
            public Type doVisit(BinaryOpNode node) {
                Type leftType = visit(node.getOperandA());
                Type rightType = visit(node.getOperandB());
                return Type.promote(leftType, rightType);
            }

            @Override
            public Type doVisit(UnaryOpNode node) {
                return visit(node.getOperand());
            }
        }.visit(rootNode);
    }

    protected <I extends Expr<Long, I>, F extends Expr<Double, F>> Expr buildExpr(ExprNode rootNode) {
        ExprBuilderFactory<I, F> factory = getExprBuilderFactory();

        PrimitiveOpBuilder<Long, I> integerOpBuilder = factory.createIntegerOpBuilder();
        PrimitiveOpBuilder<Double, F> floatOpBuilder = factory.createFloatOpBuilder();
        PrimitiveCastBuilder<I, F> primitiveCastBuilder = factory.createPrimitiveCastBuilder();

        ExprVisitor<Long, I> integerExprVisitor = new ExprVisitor<>(integerOpBuilder);
        ExprVisitor<Double, F> floatExprVisitor = new ExprVisitor<>(floatOpBuilder);

        Function<Node, I> visitInteger = integerExprVisitor::visit;
        Function<Node, F> visitFloat = floatExprVisitor::visit;

        Map<Type, Function<Node, I>> visitIntegerMap = new EnumMap<Type, Function<Node, I>>(
                Type.class) {{
            put(Type.INTEGER, visitInteger);
            put(Type.FLOAT, visitFloat.andThen(primitiveCastBuilder::toInteger));
            put(Type.UNKNOWN, node -> null);
        }};

        Map<Type, Function<Node, F>> visitFloatMap = new EnumMap<Type, Function<Node, F>>(
                Type.class) {{
            put(Type.INTEGER, visitInteger.andThen(primitiveCastBuilder::toFloat));
            put(Type.FLOAT, visitFloat);
            put(Type.UNKNOWN, node -> null);
        }};

        integerExprVisitor.setDelegate(node -> visitIntegerMap.get(getNodeType(node)).apply(node));
        floatExprVisitor.setDelegate(node -> visitFloatMap.get(getNodeType(node)).apply(node));

        switch (getNodeType(rootNode)) {
            case INTEGER:
                return integerExprVisitor.visit(rootNode);
            case FLOAT:
                return floatExprVisitor.visit(rootNode);
            case UNKNOWN:
            default:
                return null;
        }
    }

    @SuppressWarnings("unchecked")
    private <I extends Expr<Long, I>, F extends Expr<Double, F>> ExprBuilderFactory<I, F> getExprBuilderFactory() {
        return (ExprBuilderFactory<I, F>) this.exprBuilderFactory;
    }

    private Type getNodeType(ExprNode node) {
        return typeMap.getOrDefault(node, Type.UNKNOWN);
    }

}

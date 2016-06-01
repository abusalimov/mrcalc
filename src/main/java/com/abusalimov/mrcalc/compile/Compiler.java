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
import com.abusalimov.mrcalc.compile.exprtree.Expr;
import com.abusalimov.mrcalc.compile.exprtree.FloatExpr;
import com.abusalimov.mrcalc.compile.exprtree.IntegerExpr;
import com.abusalimov.mrcalc.compile.exprtree.Type;
import com.abusalimov.mrcalc.diagnostic.AbstractDiagnosticEmitter;
import com.abusalimov.mrcalc.diagnostic.Diagnostic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Eldar Abusalimov
 */
public class Compiler extends AbstractDiagnosticEmitter {

    private Map<String, VarDefStmtNode> varDefMap = new HashMap<>();
    private Map<Node, Type> typeMap = new HashMap<>();

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

    protected Expr buildExpr(ExprNode node) {
        return new NodeVisitor<Expr>() {
            @Override
            public Expr doVisit(VarRefNode node) {
                VarDefStmtNode linkedDef = node.getLinkedDef();
                if (linkedDef == null) {
                    return Expr.INVALID;
                }
                return visit(linkedDef.getExpr());
            }

            @Override
            public Expr doVisit(IntegerLiteralNode node) {
                Long l = node.getValue();
                return (IntegerExpr) () -> l;
            }

            @Override
            public Expr doVisit(FloatLiteralNode node) {
                Double d = node.getValue();
                return (FloatExpr) () -> d;
            }

            @Override
            public Expr doVisit(BinaryOpNode node) {
                Expr leftOperand = visit(node.getOperandA());
                Expr rightOperand = visit(node.getOperandB());

                Type retType = Type.promote(leftOperand.getType(), rightOperand.getType());

                leftOperand = leftOperand.cast(retType);
                rightOperand = rightOperand.cast(retType);

                switch (retType) {
                    case INTEGER: {
                        IntegerExpr left = (IntegerExpr) leftOperand;
                        IntegerExpr right = (IntegerExpr) rightOperand;

                        switch (node.getOp()) {
                            case ADD:
                                return (IntegerExpr) () -> left.getAsLong() + right.getAsLong();
                            case SUB:
                                return (IntegerExpr) () -> left.getAsLong() - right.getAsLong();
                            case MUL:
                                return (IntegerExpr) () -> left.getAsLong() * right.getAsLong();
                            case DIV:
                                return (IntegerExpr) () -> left.getAsLong() / right.getAsLong();
                            case POW:
                                return (IntegerExpr) () ->
                                        (long) Math.pow(left.getAsLong(), right.getAsLong());
                        }
                    }
                    case FLOAT: {
                        FloatExpr left = (FloatExpr) leftOperand;
                        FloatExpr right = (FloatExpr) rightOperand;

                        switch (node.getOp()) {
                            case ADD:
                                return (FloatExpr) () -> left.getAsDouble() + right.getAsDouble();
                            case SUB:
                                return (FloatExpr) () -> left.getAsDouble() - right.getAsDouble();
                            case MUL:
                                return (FloatExpr) () -> left.getAsDouble() * right.getAsDouble();
                            case DIV:
                                return (FloatExpr) () -> left.getAsDouble() / right.getAsDouble();
                            case POW:
                                return (FloatExpr) () ->
                                        Math.pow(left.getAsDouble(), right.getAsDouble());
                        }
                    }
                    case UNKNOWN:
                    default:
                        return Expr.INVALID;
                }
            }

            @Override
            public Expr doVisit(UnaryOpNode node) {
                Expr operandExpr = visit(node.getOperand());

                Type retType = operandExpr.getType();

                switch (node.getOp()) {
                    case MINUS:
                        switch (retType) {
                            case INTEGER:
                                IntegerExpr integerExpr = (IntegerExpr) operandExpr;
                                return (IntegerExpr) () -> -integerExpr.getAsLong();
                            case FLOAT:
                                FloatExpr floatExpr = (FloatExpr) operandExpr;
                                return (FloatExpr) () -> -floatExpr.getAsDouble();
                            default:
                            case UNKNOWN:
                                return Expr.INVALID;
                        }
                    default:
                    case PLUS:
                        return operandExpr;
                }
            }

        }.visit(node);
    }

    private Type getNodeType(Node node) {
        return typeMap.getOrDefault(node, Type.UNKNOWN);
    }

}

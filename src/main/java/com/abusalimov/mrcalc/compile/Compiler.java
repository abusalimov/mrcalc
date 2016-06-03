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

    @SuppressWarnings("unchecked")
    protected Expr buildExpr(ExprNode node) {
        ExprBuilder builder = new FuncExprBuilder();

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
                return builder.integerConst(node.getValue());
            }

            @Override
            public Expr doVisit(FloatLiteralNode node) {
                return builder.floatConst(node.getValue());
            }

            private Expr castExpr(Expr expr, Type toType) {
                if (expr.getType() == toType) {
                    return expr;
                }

                switch (toType) {
                    case INTEGER:
                        return builder.integerFromFloat((FloatExpr) expr);
                    case FLOAT:
                        return builder.floatFromInteger((IntegerExpr) expr);
                    case UNKNOWN:
                    default:
                        return builder.invalid();
                }
            }

            @Override
            public Expr doVisit(BinaryOpNode node) {
                Expr leftOperand = visit(node.getOperandA());
                Expr rightOperand = visit(node.getOperandB());

                Type retType = Type.promote(leftOperand.getType(), rightOperand.getType());

                leftOperand = castExpr(leftOperand, retType);
                rightOperand = castExpr(rightOperand, retType);

                switch (retType) {
                    case INTEGER: {
                        IntegerExpr left = (IntegerExpr) leftOperand;
                        IntegerExpr right = (IntegerExpr) rightOperand;

                        switch (node.getOp()) {
                            case ADD:
                                return builder.integerAdd(left, right);
                            case SUB:
                                return builder.integerSub(left, right);
                            case MUL:
                                return builder.integerMul(left, right);
                            case DIV:
                                return builder.integerDiv(left, right);
                            case POW:
                                return builder.integerPow(left, right);
                        }
                    }
                    case FLOAT: {
                        FloatExpr left = (FloatExpr) leftOperand;
                        FloatExpr right = (FloatExpr) rightOperand;

                        switch (node.getOp()) {
                            case ADD:
                                return builder.floatAdd(left, right);
                            case SUB:
                                return builder.floatSub(left, right);
                            case MUL:
                                return builder.floatMul(left, right);
                            case DIV:
                                return builder.floatDiv(left, right);
                            case POW:
                                return builder.floatPow(left, right);
                        }
                    }
                    case UNKNOWN:
                    default:
                        return Expr.INVALID;
                }
            }

            @Override
            public Expr doVisit(UnaryOpNode node) {
                Expr expr = visit(node.getOperand());

                if (node.getOp() == UnaryOpNode.Op.MINUS) {
                    switch (expr.getType()) {
                        case INTEGER:
                            return builder.integerNeg((IntegerExpr) expr);
                        case FLOAT:
                            return builder.floatNeg((FloatExpr) expr);
                    }
                }

                return expr;
            }

        }.visit(node);
    }

    private Type getNodeType(Node node) {
        return typeMap.getOrDefault(node, Type.UNKNOWN);
    }

}

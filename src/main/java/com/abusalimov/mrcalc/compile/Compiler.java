package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.ast.NodeVisitor;
import com.abusalimov.mrcalc.ast.ProgramNode;
import com.abusalimov.mrcalc.ast.expr.ExprNode;
import com.abusalimov.mrcalc.ast.expr.VarRefNode;
import com.abusalimov.mrcalc.ast.stmt.ExprStmtNode;
import com.abusalimov.mrcalc.ast.stmt.StmtNode;
import com.abusalimov.mrcalc.ast.stmt.VarDefStmtNode;
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

    public Code compile(ProgramNode node) throws CompileErrorException {
        try (ListenerClosable<CompileErrorException> ignored =
                     collectDiagnosticsToThrow(CompileErrorException::new)) {
            collectVariables(node);

            List<StmtNode> stmts = node.getStmts();

            ExprNode exprNode = null;
            if (!stmts.isEmpty()) {
                StmtNode lastStmt = stmts.get(stmts.size() - 1);
                if (lastStmt instanceof ExprStmtNode) {
                    exprNode = ((ExprStmtNode) lastStmt).getExpr();
                }
            }

            return new Code(exprNode);
        }
    }

    public Code compileExpr(ExprNode node) throws CompileErrorException {
        return new Code(node);
    }

    protected void collectVariables(ProgramNode node) {
        new NodeVisitor<Void>() {
            @Override
            public Void doVisit(VarDefStmtNode node) {
                if (varDefMap.putIfAbsent(node.getName(), node) != null) {
                    emitDiagnostic(new Diagnostic(node.getLocation(),
                            String.format("Variable '%s' is already defined", node.getName())));
                }
                return visit(node.getValue());
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
        }.visit(node);
    }
}
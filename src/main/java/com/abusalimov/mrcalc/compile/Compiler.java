package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.ast.ProgramNode;
import com.abusalimov.mrcalc.ast.expr.ExprNode;
import com.abusalimov.mrcalc.ast.stmt.ExprStmtNode;
import com.abusalimov.mrcalc.ast.stmt.StmtNode;
import com.abusalimov.mrcalc.diagnostic.AbstractDiagnosticEmitter;

import java.util.List;

/**
 * @author Eldar Abusalimov
 */
public class Compiler extends AbstractDiagnosticEmitter {

    public Code compile(ProgramNode node) throws CompileErrorException {
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

    public Code compileExpr(ExprNode node) throws CompileErrorException {
        return new Code(node);
    }
}

package com.abusalimov.mrcalc.parse.impl.antlr;

import com.abusalimov.mrcalc.ast.Node;
import com.abusalimov.mrcalc.ast.ProgramNode;
import com.abusalimov.mrcalc.ast.expr.BinaryOpNode;
import com.abusalimov.mrcalc.ast.expr.ExprNode;
import com.abusalimov.mrcalc.ast.expr.UnaryOpNode;
import com.abusalimov.mrcalc.ast.expr.VarRefNode;
import com.abusalimov.mrcalc.ast.expr.literal.FloatLiteralNode;
import com.abusalimov.mrcalc.ast.expr.literal.IntegerLiteralNode;
import com.abusalimov.mrcalc.ast.stmt.ExprStmtNode;
import com.abusalimov.mrcalc.ast.stmt.StmtNode;
import com.abusalimov.mrcalc.ast.stmt.VarDefStmtNode;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Eldar Abusalimov
 */
public class ASTConstructor extends CalcBaseVisitor<Node> {

    protected static Node initLocation(ParserRuleContext ruleContext, Node node) {
        node.setLocation(new RuleLocation(ruleContext));
        return node;
    }

    protected static Node initLocation(Token token, Node node) {
        node.setLocation(new TokenLocation(token));
        return node;
    }

    @Override
    public Node visitProgram(CalcParser.ProgramContext ctx) {
        List<StmtNode> stmtNodes = ctx.stmt().stream()
                .map((stmtContext) -> (StmtNode) visit(stmtContext)).collect(Collectors.toList());
        return initLocation(ctx, new ProgramNode(stmtNodes));
    }

    @Override
    public Node visitExprStmt(CalcParser.ExprStmtContext ctx) {
        return initLocation(ctx, new ExprStmtNode((ExprNode) visit(ctx.expr())));
    }

    @Override
    public Node visitVarDefStmt(CalcParser.VarDefStmtContext ctx) {
        return initLocation(ctx.name, new VarDefStmtNode(ctx.name.getText(),
                (ExprNode) visit(ctx.expr())));
    }

    @Override
    public Node visitNumber(CalcParser.NumberContext ctx) {
        if (ctx.value instanceof Long) {
            return initLocation(ctx, new IntegerLiteralNode((Long) ctx.value));
        } else if (ctx.value instanceof Double) {
            return initLocation(ctx, new FloatLiteralNode((Double) ctx.value));
        } else {
            throw new RuntimeException("Unknown literal type");
        }
    }

    @Override
    public Node visitVarRefExpr(CalcParser.VarRefExprContext ctx) {
        return initLocation(ctx, new VarRefNode(ctx.name.getText()));
    }

    @Override
    public Node visitUnaryOpExpr(CalcParser.UnaryOpExprContext ctx) {
        return initLocation(ctx, new UnaryOpNode(UnaryOpNode.Op.valueOfSign(ctx.op.getText()),
                (ExprNode) visit(ctx.expr())));
    }

    @Override
    public Node visitBinaryOpExpr(CalcParser.BinaryOpExprContext ctx) {
        return initLocation(ctx, new BinaryOpNode(BinaryOpNode.Op.valueOfSign(ctx.op.getText()),
                (ExprNode) visit(ctx.a), (ExprNode) visit(ctx.b)));
    }

    @Override
    protected Node aggregateResult(Node aggregate, Node nextResult) {
        return nextResult != null ? nextResult : aggregate;
    }
}

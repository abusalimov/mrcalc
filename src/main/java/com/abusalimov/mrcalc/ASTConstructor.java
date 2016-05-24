package com.abusalimov.mrcalc;

import com.abusalimov.mrcalc.ast.*;
import com.abusalimov.mrcalc.grammar.CalcBaseVisitor;
import com.abusalimov.mrcalc.grammar.CalcParser;

/**
 * @author Eldar Abusalimov
 */
public class ASTConstructor extends CalcBaseVisitor<Node> {

    @Override
    public Node visitNumber(CalcParser.NumberContext ctx) {
        if (ctx.value instanceof Long) {
            return new LongLiteralNode((Long) ctx.value);
        } else {
            throw new RuntimeException("Not implemented yet");
        }
    }

    @Override
    public Node visitUnaryOpExpr(CalcParser.UnaryOpExprContext ctx) {
        return new UnaryOpNode(UnaryOpNode.Op.valueOfSign(ctx.op.getText()),
                (ExprNode) visit(ctx.expr()));
    }

    @Override
    public Node visitBinaryOpExpr(CalcParser.BinaryOpExprContext ctx) {
        return new BinaryOpNode(BinaryOpNode.Op.valueOfSign(ctx.op.getText()),
                (ExprNode) visit(ctx.a), (ExprNode) visit(ctx.b));
    }

    @Override
    protected Node aggregateResult(Node aggregate, Node nextResult) {
        return nextResult != null ? nextResult : aggregate;
    }

}

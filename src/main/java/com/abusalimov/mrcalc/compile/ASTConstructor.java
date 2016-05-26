package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.ast.Node;
import com.abusalimov.mrcalc.ast.expr.BinaryOpNode;
import com.abusalimov.mrcalc.ast.expr.ExprNode;
import com.abusalimov.mrcalc.ast.expr.LongLiteralNode;
import com.abusalimov.mrcalc.ast.expr.UnaryOpNode;
import com.abusalimov.mrcalc.grammar.CalcBaseVisitor;
import com.abusalimov.mrcalc.grammar.CalcParser;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * @author Eldar Abusalimov
 */
public class ASTConstructor extends CalcBaseVisitor<Node> {

    protected static Node initLocation(ParserRuleContext ruleContext, Node node) {
        node.setLocation(new RuleLocation(ruleContext));
        return node;
    }

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

package com.abusalimov.mrcalc;

import com.abusalimov.mrcalc.ast.LiteralNode;
import com.abusalimov.mrcalc.ast.Node;
import com.abusalimov.mrcalc.grammar.CalcBaseVisitor;
import com.abusalimov.mrcalc.grammar.CalcParser;

/**
 * @author Eldar Abusalimov
 */
public class ASTConstructor extends CalcBaseVisitor<Node> {

    @Override
    public Node visitNumber(CalcParser.NumberContext ctx) {
        return new LiteralNode<>(ctx.value);
    }

    @Override
    protected Node aggregateResult(Node aggregate, Node nextResult) {
        return nextResult != null ? nextResult : aggregate;
    }

}

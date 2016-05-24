package com.abusalimov.mrcalc;

import com.abusalimov.mrcalc.ast.LiteralNode;
import com.abusalimov.mrcalc.ast.NodeVisitor;
import com.abusalimov.mrcalc.compile.Code;

/**
 * @author Eldar Abusalimov
 */
public class Interpreter implements NodeVisitor<Long> {
    @Override
    public Long doVisit(LiteralNode node) {
        return (Long) node.getValue();
    }

    public Long eval(Code code) {
        return visit(code.getExprNode());
    }
}

package com.abusalimov.mrcalc.ast;

import com.abusalimov.mrcalc.ast.expr.ExprNode;

import java.util.Collections;
import java.util.List;

/**
 * Some non-expression AST {@link Node}s have an intrinsic notion of an expression anyway: variable
 * definitions have an initialization expression, "print" obviously has an expression to print (even
 * thought neither statement is technically an "expression" statement), and lambda functions return
 * an expression too.
 *
 * @author Eldar Abusalimov
 */
public abstract class ExprHolderNode extends AbstractNode {
    private ExprNode expr;

    public ExprHolderNode() {
    }

    public ExprHolderNode(ExprNode expr) {
        this.expr = expr;
    }

    public ExprNode getExpr() {
        return expr;
    }

    public void setExpr(ExprNode expr) {
        this.expr = expr;
    }

    @Override
    public List<? extends ExprNode> getChildren() {
        return Collections.singletonList(expr);
    }
}

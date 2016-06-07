package com.abusalimov.mrcalc.ast;

import com.abusalimov.mrcalc.ast.expr.ExprNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Lambda is a special node type, it is not an {@link ExprNode expression}, nor a {@link
 * com.abusalimov.mrcalc.ast.stmt.StmtNode statement}. Lambdas create an isolated scope of its
 * arguments, and the lambda expression can only reference the arguments (there is no closure). In
 * general, lambda function may have an arbitrary number of arguments, though the proper arity is
 * statically checked upon usage of the lambda.
 *
 * @author Eldar Abusalimov
 */
public class LambdaNode extends ExprHolderNode {
    private List<String> argNames;

    public LambdaNode() {
        this.argNames = new ArrayList<>();
    }

    public LambdaNode(List<String> argNames, ExprNode expr) {
        super(expr);
        this.argNames = argNames;
    }

    public List<String> getArgNames() {
        return argNames;
    }

    public void setArgNames(List<String> argNames) {
        this.argNames = argNames;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.doVisit(this);
    }
}

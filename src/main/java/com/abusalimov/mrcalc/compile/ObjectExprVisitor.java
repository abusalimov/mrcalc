package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.ast.Node;
import com.abusalimov.mrcalc.ast.NodeVisitor;
import com.abusalimov.mrcalc.ast.expr.ExprNode;
import com.abusalimov.mrcalc.ast.expr.RangeNode;
import com.abusalimov.mrcalc.ast.expr.VarRefNode;
import com.abusalimov.mrcalc.backend.Expr;
import com.abusalimov.mrcalc.backend.ObjectMath;
import com.abusalimov.mrcalc.backend.impl.exprfunc.FuncExpr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Eldar Abusalimov
 */
public class ObjectExprVisitor<T, E extends Expr, I extends Expr> implements
        NodeVisitor<E> {
    private final ObjectMath<T, E, I> builder;
    private final Map<String, Integer> varIndices;
    private Function<ExprNode, I> delegate;

    public ObjectExprVisitor(ObjectMath<T, E, I> builder, List<Variable> variables) {
        this.builder = builder;
        this.varIndices = varListToIndices(variables);
    }

    protected Map<String, Integer> varListToIndices(List<Variable> variables) {
        Map<String, Integer> ret = new HashMap<>();

        int i = 0;
        for (Variable variable : variables) {
            ret.put(variable.getName(), i++);
        }

        return ret;
    }

    public Function<Object[], T> buildFunction(ExprNode node) {
        return (Function<Object[], T>) builder.toFunction(visit(node));
    }

    @Override
    public E doVisit(VarRefNode node) {
        return builder.load(varIndices.get(node.getName()), node.getName());
    }

    @Override
    public E doVisit(RangeNode node) {
        I startOperand = delegateVisit(node.getStart());
        I endOperand = delegateVisit(node.getEnd());

        // TODO stub
        return (E) (FuncExpr) args -> {
            throw new UnsupportedOperationException("NIY");
        };
    }

    @Override
    public E doVisit(ExprNode node) {
        // TODO stub
        return (E) (FuncExpr) args -> {
            throw new UnsupportedOperationException("NIY");
        };
    }

    @Override
    public E doVisit(Node node) {
        throw new UnsupportedOperationException("Expressions only");
    }

    protected I delegateVisit(ExprNode node) {
        return delegate.apply(node);
    }

    public Function<ExprNode, I> getDelegate() {
        return delegate;
    }

    public void setDelegate(Function<ExprNode, I> delegate) {
        this.delegate = delegate;
    }
}

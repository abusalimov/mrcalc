package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.ast.NodeVisitor;
import com.abusalimov.mrcalc.ast.expr.ExprNode;
import com.abusalimov.mrcalc.ast.expr.RangeNode;
import com.abusalimov.mrcalc.ast.expr.VarRefNode;
import com.abusalimov.mrcalc.ast.stmt.StmtNode;
import com.abusalimov.mrcalc.compile.exprtree.Expr;
import com.abusalimov.mrcalc.compile.exprtree.ObjectOpBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Eldar Abusalimov
 */
public class ObjectExprVisitor<T, E extends Expr<T>, I extends Expr<Long>> implements
        NodeVisitor<E> {
    private final ObjectOpBuilder<T, E, I> builder;
    private final Map<String, Integer> varIndices;
    private Function<ExprNode, I> delegate;

    public ObjectExprVisitor(ObjectOpBuilder<T, E, I> builder, List<Variable> variables) {
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
        return builder.toFunction(visit(node));
    }

    @Override
    public E doVisit(VarRefNode node) {
        return builder.load(node.getName(), varIndices.get(node.getName()));
    }

    @Override
    public E doVisit(RangeNode node) {
        I startOperand = delegateVisit(node.getStart());
        I endOperand = delegateVisit(node.getEnd());

        return builder.range(startOperand, endOperand);
    }

    @Override
    public E doVisit(StmtNode node) {
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

package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.ast.NodeVisitor;
import com.abusalimov.mrcalc.ast.expr.BinaryOpNode;
import com.abusalimov.mrcalc.ast.expr.ExprNode;
import com.abusalimov.mrcalc.ast.expr.UnaryOpNode;
import com.abusalimov.mrcalc.ast.expr.VarRefNode;
import com.abusalimov.mrcalc.ast.expr.literal.LiteralNode;
import com.abusalimov.mrcalc.ast.stmt.StmtNode;
import com.abusalimov.mrcalc.backend.Expr;
import com.abusalimov.mrcalc.backend.PrimitiveOpBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Eldar Abusalimov
 */
public class ExprVisitor<T extends Number, E extends Expr<T>> implements NodeVisitor<E> {
    private final PrimitiveOpBuilder<T, E> builder;
    private final Map<String, Integer> varIndices;
    private Function<ExprNode, E> delegate;

    public ExprVisitor(PrimitiveOpBuilder<T, E> builder, List<Variable> variables) {
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
    public E doVisit(LiteralNode<?> node) {
        return builder.constant((T) node.getValue());
    }

    @Override
    public E doVisit(BinaryOpNode node) {
        E leftOperand = delegateVisit(node.getOperandA());
        E rightOperand = delegateVisit(node.getOperandB());

        switch (node.getOp()) {
            case ADD:
                return builder.add(leftOperand, rightOperand);
            case SUB:
                return builder.sub(leftOperand, rightOperand);
            case MUL:
                return builder.mul(leftOperand, rightOperand);
            case DIV:
                return builder.div(leftOperand, rightOperand);
            case POW:
                return builder.pow(leftOperand, rightOperand);
        }
        return null;
    }

    @Override
    public E doVisit(UnaryOpNode node) {
        E expr = delegateVisit(node.getOperand());

        if (node.getOp() == UnaryOpNode.Op.MINUS) {
            return builder.neg(expr);
        }

        return expr;
    }

    @Override
    public E doVisit(StmtNode node) {
        throw new UnsupportedOperationException("Expressions only");
    }

    protected E delegateVisit(ExprNode node) {
        return delegate.apply(node);
    }

    public Function<ExprNode, E> getDelegate() {
        return delegate;
    }

    public void setDelegate(Function<ExprNode, E> delegate) {
        this.delegate = delegate;
    }
}

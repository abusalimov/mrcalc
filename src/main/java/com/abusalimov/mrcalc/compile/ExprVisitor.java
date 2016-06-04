package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.ast.NodeVisitor;
import com.abusalimov.mrcalc.ast.expr.BinaryOpNode;
import com.abusalimov.mrcalc.ast.expr.ExprNode;
import com.abusalimov.mrcalc.ast.expr.UnaryOpNode;
import com.abusalimov.mrcalc.ast.expr.VarRefNode;
import com.abusalimov.mrcalc.ast.expr.literal.LiteralNode;
import com.abusalimov.mrcalc.ast.stmt.VarDefStmtNode;
import com.abusalimov.mrcalc.compile.exprtree.Expr;
import com.abusalimov.mrcalc.compile.exprtree.PrimitiveOpBuilder;

import java.util.function.Function;

/**
 * @author Eldar Abusalimov
 */
public class ExprVisitor<T extends Number, E extends Expr<T, E>> implements NodeVisitor<E> {
    private final PrimitiveOpBuilder<T, E> builder;
    private Function<ExprNode, E> delegate;

    public ExprVisitor(PrimitiveOpBuilder<T, E> builder) {
        this.builder = builder;
    }

    protected E delegateVisit(ExprNode node) {
        return delegate.apply(node);
    }

    @Override
    public E doVisit(VarRefNode node) {
        VarDefStmtNode linkedDef = node.getLinkedDef();
        if (linkedDef == null) {
            return null;
        }
        return delegateVisit(linkedDef.getExpr());
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

    public Function<ExprNode, E> getDelegate() {
        return delegate;
    }

    public void setDelegate(Function<ExprNode, E> delegate) {
        this.delegate = delegate;
    }
}

package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.ast.ExprHolderNode;
import com.abusalimov.mrcalc.ast.Node;
import com.abusalimov.mrcalc.ast.NodeArgVisitor;
import com.abusalimov.mrcalc.ast.expr.*;
import com.abusalimov.mrcalc.ast.expr.literal.LiteralNode;
import com.abusalimov.mrcalc.backend.*;
import com.abusalimov.mrcalc.compile.type.Primitive;
import com.abusalimov.mrcalc.compile.type.Type;
import com.abusalimov.mrcalc.runtime.Evaluable;

import java.util.Objects;

/**
 * Expression builder turns a valid expression into a callable function.
 * <p>
 * The actual callable assembling is performed by the backend, and the {@code ExprBuilder} if primarily responsible for
 * delegating that to proper backend methods, based on the {@link ExprTypeInfo type info} provided among the
 * expression.
 *
 * @author Eldar Abusalimov
 */
public class ExprBuilder<E extends Expr> implements NodeArgVisitor<E, ExprTypeInfo> {
    private final Backend<E> backend;

    /**
     * Creates a new {@code ExprBuilder} assembling the expression using the specified backend.
     *
     * @param backend the backend implementation
     */
    public ExprBuilder(Backend<E> backend) {
        this.backend = Objects.requireNonNull(backend);
    }

    /**
     * Assembles the expression held by the specified {@link ExprTypeInfo} into a callable function.
     *
     * @param eti the expression with its complete type info inferred.
     * @return the assembled callable
     * @throws IllegalArgumentException if the {@code ExprTypeInfo} provided is {@link ExprTypeInfo#isComplete()
     *                                  incomplete}
     */
    public Evaluable<?> buildFunction(ExprTypeInfo eti) {
        if (!eti.isComplete()) {
            throw new IllegalArgumentException("Incomplete ExprTypeInfo");
        }
        E expr = build(eti);
        return getObjectMath(eti.getExprType()).toFunction(expr);
    }

    protected E buildChild(ExprTypeInfo parent, ExprHolderNode node) {
        ExprTypeInfo eti = parent.getChild(node);
        return build(eti);
    }

    protected E build(ExprTypeInfo eti) {
        return visit(eti.getExprNode(), eti);
    }

    protected E visitCast(ExprNode resultNode, ExprNode nodeToVisit, ExprTypeInfo eti) {
        E expr = visit(nodeToVisit, eti);
        return getNumberCast(resultNode, nodeToVisit, eti).cast(expr);
    }

    @Override
    public E doVisit(VarRefNode node, ExprTypeInfo eti) {
        String name = node.getName();
        return getObjectMath(node, eti).load(eti.getReferencedVariableIndex(name), name);
    }

    @Override
    public E doVisit(LiteralNode<?> node, ExprTypeInfo eti) {
        return getObjectMath(node, eti).constant(node.getValue());
    }

    @Override
    public E doVisit(BinaryOpNode node, ExprTypeInfo eti) {
        E leftOperand = visitCast(node, node.getOperandA(), eti);
        E rightOperand = visitCast(node, node.getOperandB(), eti);

        switch (node.getOp()) {
            case ADD:
                return getNumberMath(eti.getExprType(node)).add(leftOperand, rightOperand);
            case SUB:
                return getNumberMath(eti.getExprType(node)).sub(leftOperand, rightOperand);
            case MUL:
                return getNumberMath(eti.getExprType(node)).mul(leftOperand, rightOperand);
            case DIV:
                return getNumberMath(eti.getExprType(node)).div(leftOperand, rightOperand);
            case POW:
                return getNumberMath(eti.getExprType(node)).pow(leftOperand, rightOperand);
        }
        return null;
    }

    @Override
    public E doVisit(UnaryOpNode node, ExprTypeInfo eti) {
        E expr = visitCast(node, node.getOperand(), eti);

        if (node.getOp() == UnaryOpNode.Op.MINUS) {
            return getNumberMath(node, eti).neg(expr);
        }

        return expr;
    }

    @Override
    public E doVisit(RangeNode node, ExprTypeInfo eti) {
        E startOperand = visit(node.getStart(), eti);
        E endOperand = visit(node.getEnd(), eti);

        return getNumberMath(Primitive.INTEGER).range(startOperand, endOperand);
    }

    @Override
    public E doVisit(MapNode node, ExprTypeInfo eti) {
        E sequence = visit(node.getSequence(), eti);
        E lambda = buildChild(eti, node.getLambda());

        return getObjectMath(eti.getChild(node.getLambda()).getExprType()).map(sequence, lambda);
    }

    @Override
    public E doVisit(ReduceNode node, ExprTypeInfo eti) {
        E sequence = visit(node.getSequence(), eti);
        E neutral = visit(node.getNeutral(), eti);
        E lambda = buildChild(eti, node.getLambda());

        return getObjectMath(node, eti).reduce(sequence, neutral, lambda);
    }

    private <T> ObjectMath<T, E, E> getObjectMath(ExprNode node, ExprTypeInfo eti) {
        return getObjectMath(eti.getExprType(node));
    }

    @SuppressWarnings("unchecked")
    private <T> ObjectMath<T, E, E> getObjectMath(Type exprType) {
        return backend.getObjectMath((Class<T>) exprType.getTypeClass());
    }

    private <T extends Number> NumberMath<T, E, E> getNumberMath(ExprNode node, ExprTypeInfo eti) {
        return getNumberMath(eti.getExprType(node));
    }

    @SuppressWarnings("unchecked")
    private <T extends Number> NumberMath<T, E, E> getNumberMath(Type exprType) {
        return backend.getNumberMath((Class<T>) exprType.getTypeClass());
    }

    private NumberCast<E, E> getNumberCast(ExprNode toNode, ExprNode fromNode, ExprTypeInfo eti) {
        return getNumberCast(eti.getExprType(toNode), eti.getExprType(fromNode));
    }

    private NumberCast<E, E> getNumberCast(Type toType, Type fromType) {
        if (!(toType instanceof Primitive && fromType instanceof Primitive)) {
            throw new IllegalArgumentException("Nodes of primitive types expected");
        }
        Primitive toPrimitive = (Primitive) toType;
        Primitive fromPrimitive = (Primitive) fromType;

        if (fromPrimitive == toPrimitive) {
            return expr -> expr;
        }

        return backend.getNumberCast(toPrimitive.getTypeClass(), fromPrimitive.getTypeClass());
    }

    @Override
    public E doVisit(Node node, ExprTypeInfo eti) {
        throw new UnsupportedOperationException("Expressions only");
    }
}

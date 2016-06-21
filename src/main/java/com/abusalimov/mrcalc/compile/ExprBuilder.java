package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.ast.ExprHolderNode;
import com.abusalimov.mrcalc.ast.Node;
import com.abusalimov.mrcalc.ast.NodeArgVisitor;
import com.abusalimov.mrcalc.ast.expr.*;
import com.abusalimov.mrcalc.ast.expr.literal.LiteralNode;
import com.abusalimov.mrcalc.backend.*;
import com.abusalimov.mrcalc.compile.type.PrimitiveType;
import com.abusalimov.mrcalc.compile.type.SequenceType;
import com.abusalimov.mrcalc.compile.type.Type;
import com.abusalimov.mrcalc.runtime.Evaluable;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Expression builder turns a valid expression into a callable function.
 * <p>
 * The actual callable assembling is performed by the backend, and the {@code ExprBuilder} if primarily responsible for
 * delegating that to proper backend methods, based on the {@link ExprTypeInfo type info} provided among the
 * expression.
 *
 * @param <E> the internal type of expressions used by the backend
 * @param <F> the internal type of assembled functions used by the backend
 * @author Eldar Abusalimov
 */
public class ExprBuilder<E, F> implements NodeArgVisitor<E, ExprBuilder.FunctionContext<?, E, F>> {
    private final Backend<E, F> backend;

    /**
     * Creates a new {@code ExprBuilder} assembling the expression using the specified backend.
     *
     * @param backend the backend implementation
     */
    public ExprBuilder(Backend<E, F> backend) {
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
        FunctionContext<?, E, F> fctx = new FunctionContext<>(backend, eti);
        F func = build(fctx);
        return fctx.getFunctionAssembler().toEvaluable(func);
    }

    protected E buildChild(FunctionContext parent, ExprHolderNode node) {
        ExprTypeInfo eti = parent.getExprTypeInfo().getChild(node);
        FunctionContext<?, E, F> fctx = new FunctionContext<>(backend, eti);
        F func = build(fctx);
        return fctx.getFunctionAssembler().lambda(func);
    }

    protected F build(FunctionContext<?, E, F> fctx) {
        E expr = visit(fctx.getExprTypeInfo().getExprNode(), fctx);
        return fctx.getFunctionAssembler().assemble(expr);
    }

    protected E visitCast(ExprNode resultNode, ExprNode nodeToVisit, FunctionContext<?, E, F> fctx) {
        E expr = visit(nodeToVisit, fctx);
        return fctx.getNumberCast(resultNode, nodeToVisit).cast(expr);
    }

    @Override
    public E doVisit(VarRefNode node, FunctionContext<?, E, F> fctx) {
        String name = node.getName();
        return fctx.getArgumentLoad(node).load(fctx.getExprTypeInfo().getReferencedVariableIndex(name));
    }

    @Override
    public E doVisit(LiteralNode<?> node, FunctionContext<?, E, F> fctx) {
        return fctx.getNumberMath(node).constant(node.getValue());
    }

    @Override
    public E doVisit(BinaryOpNode node, FunctionContext<?, E, F> fctx) {
        E leftOperand = visitCast(node, node.getOperandA(), fctx);
        E rightOperand = visitCast(node, node.getOperandB(), fctx);

        switch (node.getOp()) {
            case ADD:
                return fctx.getNumberMath(node).add(leftOperand, rightOperand);
            case SUB:
                return fctx.getNumberMath(node).sub(leftOperand, rightOperand);
            case MUL:
                return fctx.getNumberMath(node).mul(leftOperand, rightOperand);
            case DIV:
                return fctx.getNumberMath(node).div(leftOperand, rightOperand);
            case POW:
                return fctx.getNumberMath(node).pow(leftOperand, rightOperand);
        }
        return null;
    }

    @Override
    public E doVisit(UnaryOpNode node, FunctionContext<?, E, F> fctx) {
        E expr = visitCast(node, node.getOperand(), fctx);

        if (node.getOp() == UnaryOpNode.Op.MINUS) {
            return fctx.getNumberMath(node).neg(expr);
        }

        return expr;
    }

    @Override
    public E doVisit(RangeNode node, FunctionContext<?, E, F> fctx) {
        E startOperand = visit(node.getStart(), fctx);
        E endOperand = visit(node.getEnd(), fctx);

        return fctx.getSequenceRange(node.getStart()).range(startOperand, endOperand);
    }

    @Override
    public E doVisit(MapNode node, FunctionContext<?, E, F> fctx) {
        E sequence = visit(node.getSequence(), fctx);
        E lambda = buildChild(fctx, node.getLambda());

        return fctx.getSequenceMap(node, node.getSequence()).map(sequence, lambda);
    }

    @Override
    public E doVisit(ReduceNode node, FunctionContext<?, E, F> fctx) {
        E sequence = visit(node.getSequence(), fctx);
        E neutral = visit(node.getNeutral(), fctx);
        E lambda = buildChild(fctx, node.getLambda());

        return fctx.getSequenceReduce(node).reduce(sequence, neutral, lambda);
    }

    @Override
    public E doVisit(Node node, FunctionContext<?, E, F> fctx) {
        throw new UnsupportedOperationException("Expressions only");
    }

    /**
     * Encapsulates the backend access within a context of given {@link ExprTypeInfo} and {@link FunctionAssembler}.
     *
     * @param <R> the return type of the function being assembled
     * @param <E> the internal type of expressions used by the backend
     * @param <F> the internal type of assembled functions used by the backend
     */
    static class FunctionContext<R, E, F> {
        private final Backend<E, F> backend;
        private final ExprTypeInfo exprTypeInfo;
        private final FunctionAssembler<R, E, F> functionAssembler;

        public FunctionContext(Backend<E, F> backend, ExprTypeInfo exprTypeInfo) {
            this.backend = backend;
            this.exprTypeInfo = exprTypeInfo;

            @SuppressWarnings("unchecked") Class<R> returnType = (Class<R>) getExprType().getTypeClass();
            Class<?>[] parameterTypes = exprTypeInfo.getReferencedVariables().stream()
                    .map(Variable::getType)
                    .map(Type::getTypeClass)
                    .collect(Collectors.toList()).toArray(new Class<?>[0]);

            this.functionAssembler = backend.createFunctionAssembler(returnType, parameterTypes);
        }

        public ExprTypeInfo getExprTypeInfo() {
            return exprTypeInfo;
        }

        public FunctionAssembler<R, E, F> getFunctionAssembler() {
            return functionAssembler;
        }

        public Type getExprType() {
            return exprTypeInfo.getExprType();
        }

        public Type getExprType(ExprNode node) {
            return exprTypeInfo.getExprType(node);
        }

        public ArgumentLoad<E> getArgumentLoad(ExprNode node) {
            return getArgumentLoad(getExprType(node));
        }

        public ArgumentLoad<E> getArgumentLoad(Type exprType) {
            return functionAssembler.getArgumentLoad(exprType.getTypeClass());
        }

        public <T extends Number> NumberMath<T, E> getNumberMath(ExprNode node) {
            return getNumberMath(getExprType(node));
        }

        @SuppressWarnings("unchecked")
        public <T extends Number> NumberMath<T, E> getNumberMath(Type exprType) {
            return backend.getNumberMath((Class<T>) exprType.getTypeClass());
        }

        public NumberCast<E, E> getNumberCast(ExprNode toNode, ExprNode fromNode) {
            return getNumberCast(getExprType(toNode), getExprType(fromNode));
        }

        public NumberCast<E, E> getNumberCast(Type toType, Type fromType) {
            PrimitiveType toPrimitive = checkPrimitive(toType);
            PrimitiveType fromPrimitive = checkPrimitive(fromType);

            if (fromPrimitive == toPrimitive) {
                return expr -> expr;
            }

            return backend.getNumberCast(toPrimitive.getTypeClass(), fromPrimitive.getTypeClass());
        }

        public SequenceRange<E, E> getSequenceRange(ExprNode boundaryNode) {
            return getSequenceRange(getExprType(boundaryNode));
        }

        public SequenceRange<E, E> getSequenceRange(Type elementType) {
            PrimitiveType elementPrimitive = (PrimitiveType) elementType;
            return backend.getSequenceRange(elementPrimitive.getTypeClass());
        }

        public SequenceReduce<E, E, E> getSequenceReduce(ExprNode returnNode) {
            return getSequenceReduce(getExprType(returnNode));
        }

        public SequenceReduce<E, E, E> getSequenceReduce(Type returnType) {
            return backend.getSequenceReduce(returnType.getTypeClass());
        }

        public SequenceMap<E, E, E> getSequenceMap(ExprNode returnNode, ExprNode sequenceNode) {
            return getSequenceMap(getExprType(returnNode), getExprType(sequenceNode));
        }

        public SequenceMap<E, E, E> getSequenceMap(Type returnType, Type sequenceType) {
            return backend.getSequenceMap(getSequenceElementType(returnType).getTypeClass(),
                    getSequenceElementType(sequenceType).getTypeClass());
        }

        private PrimitiveType checkPrimitive(Type type) {
            if (!(type instanceof PrimitiveType)) {
                throw new IllegalArgumentException("Node of primitive type expected");
            }
            return (PrimitiveType) type;
        }

        private Type getSequenceElementType(Type type) {
            if (!(type instanceof SequenceType)) {
                throw new IllegalArgumentException("Node of sequence type expected");
            }
            return ((SequenceType) type).getElementType();
        }
    }
}

package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.NumberMath;
import com.abusalimov.mrcalc.runtime.ObjectSequence;

/**
 * Implements numeric math on boxed {@link Long}s.
 *
 * @author Eldar Abusalimov
 */
public class LongFuncNumberMath extends FuncObjectMath<Long>
        implements NumberMath<Long, FuncExpr<Long>, FuncExpr<ObjectSequence<?>>> {
    public static final LongFuncNumberMath INSTANCE = new LongFuncNumberMath();

    @Override
    public FuncExpr<ObjectSequence<?>> range(FuncExpr<Long> startOperand, FuncExpr<Long> endOperand) {
        return (runtime, args) -> {
            long start = startOperand.apply(runtime, args);
            long end = endOperand.apply(runtime, args);
            return runtime.createLongRange(start, end + 1).mapToObject(value -> value);
        };
    }

    @Override
    public FuncExpr<Long> add(FuncExpr<Long> leftOperand, FuncExpr<Long> rightOperand) {
        return (runtime, args) -> leftOperand.apply(runtime, args) + rightOperand.apply(runtime, args);
    }

    @Override
    public FuncExpr<Long> sub(FuncExpr<Long> leftOperand, FuncExpr<Long> rightOperand) {
        return (runtime, args) -> leftOperand.apply(runtime, args) - rightOperand.apply(runtime, args);
    }

    @Override
    public FuncExpr<Long> mul(FuncExpr<Long> leftOperand, FuncExpr<Long> rightOperand) {
        return (runtime, args) -> leftOperand.apply(runtime, args) * rightOperand.apply(runtime, args);
    }

    @Override
    public FuncExpr<Long> div(FuncExpr<Long> leftOperand, FuncExpr<Long> rightOperand) {
        return (runtime, args) -> leftOperand.apply(runtime, args) / rightOperand.apply(runtime, args);
    }

    @Override
    public FuncExpr<Long> pow(FuncExpr<Long> leftOperand, FuncExpr<Long> rightOperand) {
        return (runtime, args) -> (long) Math.pow(leftOperand.apply(runtime, args), rightOperand.apply(runtime, args));
    }

    @Override
    public FuncExpr<Long> neg(FuncExpr<Long> operand) {
        return (runtime, args) -> -operand.apply(runtime, args);
    }
}

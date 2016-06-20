package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.NumberMath;
import com.abusalimov.mrcalc.runtime.LongRange;
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
        return args -> {
            long start = startOperand.apply(args);
            long end = endOperand.apply(args);
            return new LongRange(start, end + 1).mapToObject(value -> value);
        };
    }

    @Override
    public FuncExpr<Long> add(FuncExpr<Long> leftOperand, FuncExpr<Long> rightOperand) {
        return args -> leftOperand.apply(args) + rightOperand.apply(args);
    }

    @Override
    public FuncExpr<Long> sub(FuncExpr<Long> leftOperand, FuncExpr<Long> rightOperand) {
        return args -> leftOperand.apply(args) - rightOperand.apply(args);
    }

    @Override
    public FuncExpr<Long> mul(FuncExpr<Long> leftOperand, FuncExpr<Long> rightOperand) {
        return args -> leftOperand.apply(args) * rightOperand.apply(args);
    }

    @Override
    public FuncExpr<Long> div(FuncExpr<Long> leftOperand, FuncExpr<Long> rightOperand) {
        return args -> leftOperand.apply(args) / rightOperand.apply(args);
    }

    @Override
    public FuncExpr<Long> pow(FuncExpr<Long> leftOperand, FuncExpr<Long> rightOperand) {
        return args -> (long) Math.pow(leftOperand.apply(args), rightOperand.apply(args));
    }

    @Override
    public FuncExpr<Long> neg(FuncExpr<Long> operand) {
        return args -> -operand.apply(args);
    }
}

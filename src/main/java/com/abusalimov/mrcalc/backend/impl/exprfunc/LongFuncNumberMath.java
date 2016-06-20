package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.NumberMath;
import com.abusalimov.mrcalc.runtime.LongSequence;
import com.abusalimov.mrcalc.runtime.Sequence;

/**
 * Implements numeric math on boxed {@link Long}s.
 *
 * @author Eldar Abusalimov
 */
public class LongFuncNumberMath extends FuncObjectMath<Long>
        implements NumberMath<Long, FuncExpr<Long>, FuncExpr<Sequence<?>>> {
    public static final LongFuncNumberMath INSTANCE = new LongFuncNumberMath();

    @Override
    public FuncExpr<Sequence<?>> range(FuncExpr<Long> startOperand, FuncExpr<Long> endOperand) {
        return (runtime, args) -> {
            long start = startOperand.eval(runtime, args);
            long end = endOperand.eval(runtime, args);
            LongSequence longSequence = runtime.createLongRange(start, end + 1);
            return runtime.mapLongToObject(longSequence, value -> value);  /* Box primitive longs. */
        };
    }

    @Override
    public FuncExpr<Long> add(FuncExpr<Long> leftOperand, FuncExpr<Long> rightOperand) {
        return (runtime, args) -> leftOperand.eval(runtime, args) + rightOperand.eval(runtime, args);
    }

    @Override
    public FuncExpr<Long> sub(FuncExpr<Long> leftOperand, FuncExpr<Long> rightOperand) {
        return (runtime, args) -> leftOperand.eval(runtime, args) - rightOperand.eval(runtime, args);
    }

    @Override
    public FuncExpr<Long> mul(FuncExpr<Long> leftOperand, FuncExpr<Long> rightOperand) {
        return (runtime, args) -> leftOperand.eval(runtime, args) * rightOperand.eval(runtime, args);
    }

    @Override
    public FuncExpr<Long> div(FuncExpr<Long> leftOperand, FuncExpr<Long> rightOperand) {
        return (runtime, args) -> leftOperand.eval(runtime, args) / rightOperand.eval(runtime, args);
    }

    @Override
    public FuncExpr<Long> pow(FuncExpr<Long> leftOperand, FuncExpr<Long> rightOperand) {
        return (runtime, args) -> (long) Math.pow(leftOperand.eval(runtime, args), rightOperand.eval(runtime, args));
    }

    @Override
    public FuncExpr<Long> neg(FuncExpr<Long> operand) {
        return (runtime, args) -> -operand.eval(runtime, args);
    }
}

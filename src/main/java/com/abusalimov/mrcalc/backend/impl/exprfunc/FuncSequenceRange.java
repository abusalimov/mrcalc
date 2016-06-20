package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.SequenceRange;
import com.abusalimov.mrcalc.runtime.Sequence;

/**
 * Implements range-creating expressions factory.
 *
 * @author Eldar Abusalimov
 */
public class FuncSequenceRange implements SequenceRange<FuncExpr<Sequence<Long>>, FuncExpr<Long>> {
    public static final FuncSequenceRange INSTANCE = new FuncSequenceRange();

    @Override
    public FuncExpr<Sequence<Long>> range(FuncExpr<Long> startOperand, FuncExpr<Long> endOperand) {
        return (runtime, args) -> {
            long start = startOperand.eval(runtime, args);
            long end = endOperand.eval(runtime, args);
            return runtime.createLongRange(start, end + 1);
        };
    }
}

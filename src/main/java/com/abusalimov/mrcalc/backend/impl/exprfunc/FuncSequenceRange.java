package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.SequenceRange;
import com.abusalimov.mrcalc.runtime.Sequence;

/**
 * Implements range-creating expressions factory.
 *
 * @author Eldar Abusalimov
 */
public class FuncSequenceRange implements SequenceRange<Func<Sequence<Long>>, Func<Long>> {
    public static final FuncSequenceRange INSTANCE = new FuncSequenceRange();

    @Override
    public Func<Sequence<Long>> range(Func<Long> startOperand, Func<Long> endOperand) {
        return (runtime, args) -> {
            long start = startOperand.eval(runtime, args);
            long end = endOperand.eval(runtime, args);
            return runtime.createLongRange(start, end + 1);
        };
    }
}

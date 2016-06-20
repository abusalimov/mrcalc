package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.SequenceReduce;
import com.abusalimov.mrcalc.runtime.Sequence;

/**
 * Implements sequence reduction expressions factory.
 *
 * @param <T> the type of a sequence elements and a reduction result
 * @author Eldar Abusalimov
 */
public class FuncSequenceReduce<T> implements SequenceReduce<FuncExpr<T>, FuncExpr<Sequence<T>>, FuncExpr<T>> {
    public static final FuncSequenceReduce INSTANCE = new FuncSequenceReduce();

    @Override
    public FuncExpr<T> reduce(FuncExpr<Sequence<T>> sequenceExpr, FuncExpr<T> neutralExpr, FuncExpr<T> lambda) {
        return (runtime, args) -> {
            Sequence<T> sequence = sequenceExpr.eval(runtime, args);
            T neutral = neutralExpr.eval(runtime, args);
            return runtime.reduce(sequence, neutral, (x, y) -> lambda.eval(runtime, new Object[]{x, y}));
        };
    }
}

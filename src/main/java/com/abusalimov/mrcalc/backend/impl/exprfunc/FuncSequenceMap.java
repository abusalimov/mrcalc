package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.SequenceMap;
import com.abusalimov.mrcalc.runtime.Sequence;

/**
 * Implements sequence mapping expressions factory.
 *
 * @param <T> the type of a sequence elements
 * @author Eldar Abusalimov
 */
public class FuncSequenceMap<T> implements SequenceMap<FuncExpr<Sequence<T>>, FuncExpr<Sequence<T>>, FuncExpr<T>> {
    public static final FuncSequenceMap INSTANCE = new FuncSequenceMap();

    @Override
    public FuncExpr<Sequence<T>> map(FuncExpr<Sequence<T>> sequenceExpr, FuncExpr<T> lambda) {
        return (runtime, args) -> {
            Sequence<?> sequence = sequenceExpr.eval(runtime, args);
            return runtime.mapToObject(sequence, x -> lambda.eval(runtime, new Object[]{x}));
        };
    }
}

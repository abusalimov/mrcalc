package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.SequenceMap;
import com.abusalimov.mrcalc.runtime.Sequence;

/**
 * Implements sequence mapping expressions factory.
 *
 * @param <T> the type of a sequence elements
 * @author Eldar Abusalimov
 */
public class FuncSequenceMap<T> implements SequenceMap<Func<Sequence<T>>, Func<Sequence<?>>, Func<T>> {
    public static final FuncSequenceMap INSTANCE = new FuncSequenceMap();

    @Override
    public Func<Sequence<T>> map(Func<Sequence<?>> sequenceExpr, Func<T> lambda) {
        return (runtime, args) -> {
            Sequence<?> sequence = sequenceExpr.eval(runtime, args);
            return runtime.mapToObject(sequence, x -> lambda.eval(runtime, new Object[]{x}));
        };
    }
}

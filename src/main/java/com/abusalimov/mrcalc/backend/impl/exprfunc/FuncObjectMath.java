package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.ObjectMath;
import com.abusalimov.mrcalc.runtime.ObjectSequence;
import com.abusalimov.mrcalc.runtime.Runtime;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Implements operations common for all expression types, both numeric and generic.
 *
 * @author Eldar Abusalimov
 */
public class FuncObjectMath<T> implements ObjectMath<T, FuncExpr<T>, FuncExpr<ObjectSequence<?>>> {
    public static final FuncObjectMath INSTANCE = new FuncObjectMath();

    @Override
    public BiFunction<Runtime, Object[], ?> toFunction(FuncExpr<T> expr) {
        return expr;
    }

    @SuppressWarnings("unchecked")
    @Override
    public FuncExpr<T> load(int slot, String name) {
        return (runtime, args) -> (T) args[slot];
    }

    @Override
    public FuncExpr<T> constant(T literal) {
        return (runtime, args) -> literal;
    }

    @Override
    public FuncExpr<ObjectSequence<?>> map(FuncExpr<ObjectSequence<?>> sequenceExpr, FuncExpr<T> lambda) {
        return (runtime, args) -> {
            ObjectSequence<?> sequence = sequenceExpr.apply(runtime, args);
            return sequence.mapToObject(x -> lambda.apply(runtime, new Object[]{x}));
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public FuncExpr<T> reduce(FuncExpr<ObjectSequence<?>> sequenceExpr, FuncExpr<T> neutral, FuncExpr<T> lambda) {
        return (runtime, args) -> {
            ObjectSequence<T> sequence = (ObjectSequence<T>) sequenceExpr.apply(runtime, args);
            return sequence.reduce(neutral.apply(runtime, args), (x, y) -> lambda.apply(runtime, new Object[]{x, y}));
        };
    }
}

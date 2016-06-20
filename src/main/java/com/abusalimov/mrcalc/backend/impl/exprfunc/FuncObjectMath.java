package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.ObjectMath;
import com.abusalimov.mrcalc.runtime.Evaluable;
import com.abusalimov.mrcalc.runtime.Sequence;

/**
 * Implements operations common for all expression types, both numeric and generic.
 *
 * @author Eldar Abusalimov
 */
public class FuncObjectMath<T> implements ObjectMath<T, FuncExpr<T>, FuncExpr<Sequence<?>>> {
    public static final FuncObjectMath INSTANCE = new FuncObjectMath();

    @Override
    public Evaluable<?> toEvaluable(FuncExpr<T> expr) {
        return expr;
    }

    @SuppressWarnings("unchecked")
    @Override
    public FuncExpr<T> load(int slot) {
        return (runtime, args) -> (T) args[slot];
    }

    @Override
    public FuncExpr<T> constant(T literal) {
        return (runtime, args) -> literal;
    }

    @Override
    public FuncExpr<Sequence<?>> map(FuncExpr<Sequence<?>> sequenceExpr, FuncExpr<T> lambda) {
        return (runtime, args) -> {
            Sequence<?> sequence = sequenceExpr.eval(runtime, args);
            return runtime.mapToObject(sequence, x -> lambda.eval(runtime, new Object[]{x}));
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public FuncExpr<T> reduce(FuncExpr<Sequence<?>> sequenceExpr, FuncExpr<T> neutralExpr, FuncExpr<T> lambda) {
        return (runtime, args) -> {
            Sequence<T> sequence = (Sequence<T>) sequenceExpr.eval(runtime, args);
            T neutral = neutralExpr.eval(runtime, args);
            return runtime.reduce(sequence, neutral, (x, y) -> lambda.eval(runtime, new Object[]{x, y}));
        };
    }
}

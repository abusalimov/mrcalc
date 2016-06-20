package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.ObjectMath;
import com.abusalimov.mrcalc.runtime.Evaluable;

/**
 * Implements operations common for all expression types, both numeric and generic.
 *
 * @author Eldar Abusalimov
 */
public class FuncObjectMath<T> implements ObjectMath<T, FuncExpr<T>> {
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
}

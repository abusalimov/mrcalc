package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.NumberMath;

/**
 * Implements operations common for numeric expressions.
 *
 * @author Eldar Abusalimov
 */
public abstract class AbstractFuncNumberMath<T extends Number> implements NumberMath<T, Func<T>> {
    @Override
    public Func<T> constant(T literal) {
        return (runtime, args) -> literal;
    }
}

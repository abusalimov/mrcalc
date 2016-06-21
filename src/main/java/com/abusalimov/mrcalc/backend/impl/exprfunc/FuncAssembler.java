package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.ArgumentLoad;
import com.abusalimov.mrcalc.backend.FunctionAssembler;
import com.abusalimov.mrcalc.runtime.Evaluable;

/**
 * The function assembler implementation.
 *
 * @author Eldar Abusalimov
 */
public class FuncAssembler<T> implements FunctionAssembler<T, Func<?>, Func<T>> {
    @Override
    public ArgumentLoad<Func<?>> getArgumentLoad(Class<?> parameterType) {
        return (slot) -> (runtime, args) -> args[slot];
    }

    @SuppressWarnings("unchecked")
    @Override
    public Func<T> assemble(Func<?> expr) {
        return (Func<T>) expr;
    }

    @Override
    public Func<?> lambda(Func<T> function) {
        return function;
    }

    @Override
    public Evaluable<T> toEvaluable(Func<T> function) {
        return function;
    }
}

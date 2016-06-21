package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.*;
import com.abusalimov.mrcalc.runtime.Evaluable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Arrays.asList;

/**
 * The function assembler implementation.
 *
 * @author Eldar Abusalimov
 */
public class FuncAssembler<R> implements FunctionAssembler<R, Func<?>, Func<R>> {
    private static final Map<Class<?>, NumberMath<?, ?>> mathMap = new HashMap<>();
    private static final Map<List<Class<? extends Number>>, NumberCast<?, ?>> numberCastMap = new HashMap<>();

    static {
        mathMap.put(Long.TYPE, LongFuncNumberMath.INSTANCE);
        mathMap.put(Double.TYPE, DoubleFuncNumberMath.INSTANCE);
    }

    static {
        putNumberCast(Double.TYPE, Long.TYPE, expr -> (runtime, args) -> expr.eval(runtime, args).doubleValue());
        putNumberCast(Long.TYPE, Double.TYPE, expr -> (runtime, args) -> expr.eval(runtime, args).longValue());
    }

    private static <F extends Number, T extends Number> void putNumberCast(Class<T> toType, Class<F> fromType,
                                                                           NumberCast<Func<T>, Func<F>> cast) {
        numberCastMap.put(asList(toType, fromType), cast);
    }

    @Override
    public ArgumentLoad<Func<?>> getArgumentLoad(Class<?> parameterType) {
        return (slot) -> (runtime, args) -> args[slot];
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Number> NumberMath<T, Func<?>> getNumberMath(Class<T> returnType) {
        return (NumberMath<T, Func<?>>) mathMap.computeIfAbsent(
                Objects.requireNonNull(returnType, "returnType"), aClass -> {
                    throw new UnsupportedOperationException("Unknown Number class " + aClass);
                });
    }

    @SuppressWarnings("unchecked")
    @Override
    public NumberCast getNumberCast(Class<? extends Number> toType, Class<? extends Number> fromType) {
        return numberCastMap.get(asList(toType, fromType));
    }

    @SuppressWarnings("unchecked")
    @Override
    public SequenceRange getSequenceRange(Class<? extends Number> elementType) {
        return FuncSequenceRange.INSTANCE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public SequenceReduce getSequenceReduce(Class<?> returnType) {
        return FuncSequenceReduce.INSTANCE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public SequenceMap getSequenceMap(Class<?> returnElementType, Class<?> elementType) {
        return FuncSequenceMap.INSTANCE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Func<R> assemble(Func<?> expr) {
        return (Func<R>) expr;
    }

    @Override
    public Func<?> lambda(Func<R> function) {
        return function;
    }

    @Override
    public Evaluable<R> toEvaluable(Func<R> function) {
        return function;
    }
}

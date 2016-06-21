package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Arrays.asList;

/**
 * The backend implementation using functions as the expression type and evaluating expressions by calling the
 * corresponding functions.
 *
 * @author Eldar Abusalimov
 */
public class FuncBackendImpl implements Backend<Func<?>, Func<?>> {
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
                                                                           NumberCast<Func<F>, Func<T>> cast) {
        numberCastMap.put(asList(toType, fromType), cast);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> FunctionAssembler<R, Func<?>, Func<?>> createFunctionAssembler(Class<R> returnType,
                                                                              Class<?>... parameterTypes) {
        return new FuncAssembler();
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
}

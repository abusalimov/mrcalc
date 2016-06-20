package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.Backend;
import com.abusalimov.mrcalc.backend.NumberCast;
import com.abusalimov.mrcalc.backend.NumberMath;
import com.abusalimov.mrcalc.backend.ObjectMath;

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
public class FuncBackendImpl implements Backend<FuncExpr<?>> {
    private static final Map<Class<?>, ObjectMath<?, ?, ?>> mathMap = new HashMap<>();
    private static final Map<List<Class<? extends Number>>, NumberCast<?, ?>> numberCastMap = new HashMap<>();

    static {
        mathMap.put(Long.class, LongFuncNumberMath.INSTANCE);
        mathMap.put(Double.class, DoubleFuncNumberMath.INSTANCE);
    }

    static {
        putNumberCast(Double.class, Long.class, expr -> (runtime, args) -> expr.apply(runtime, args).doubleValue());
        putNumberCast(Long.class, Double.class, expr -> (runtime, args) -> expr.apply(runtime, args).longValue());
    }

    private static <F extends Number, T extends Number> void putNumberCast(Class<T> toType, Class<F> fromType,
                                                                           NumberCast<FuncExpr<F>, FuncExpr<T>> cast) {
        numberCastMap.put(asList(toType, fromType), cast);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ObjectMath<T, FuncExpr<?>, FuncExpr<?>> getObjectMath(Class<T> returnType) {
        return (ObjectMath<T, FuncExpr<?>, FuncExpr<?>>) mathMap.getOrDefault(
                Objects.requireNonNull(returnType, "returnType"), FuncObjectMath.INSTANCE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Number> NumberMath<T, FuncExpr<?>, FuncExpr<?>> getNumberMath(Class<T> returnType) {
        return (NumberMath<T, FuncExpr<?>, FuncExpr<?>>) mathMap.computeIfAbsent(
                Objects.requireNonNull(returnType, "returnType"), aClass -> {
                    throw new UnsupportedOperationException("Unknown Number class " + aClass);
                });
    }

    @SuppressWarnings("unchecked")
    @Override
    public NumberCast<FuncExpr<?>, FuncExpr<?>> getNumberCast(Class<? extends Number> toType,
                                                              Class<? extends Number> fromType) {
        return (NumberCast<FuncExpr<?>, FuncExpr<?>>) numberCastMap.get(asList(toType, fromType));
    }
}

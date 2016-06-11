package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.ExprFactory;
import com.abusalimov.mrcalc.backend.NumberCast;
import com.abusalimov.mrcalc.backend.NumberMath;
import com.abusalimov.mrcalc.backend.ObjectMath;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Arrays.asList;

/**
 * @author Eldar Abusalimov
 */
public class FuncExprFactoryImpl implements ExprFactory<FuncObjectExpr<?>> {
    private static final Map<Class<?>, ObjectMath<?, ?, ?>> mathMap = new HashMap<>();
    private static final Map<List<Class<? extends Number>>, NumberCast<?, ?>> numberCastMap = new HashMap<>();

    static {
        mathMap.put(Long.class, LongFuncNumberMath.INSTANCE);
        mathMap.put(Double.class, DoubleFuncNumberMath.INSTANCE);
    }

    static {
        putNumberCast(Double.class, Long.class, expr -> args -> expr.apply(args).doubleValue());
        putNumberCast(Long.class, Double.class, expr -> args -> expr.apply(args).longValue());
    }

    private static <F extends Number, T extends Number> void putNumberCast(Class<T> toType, Class<F> fromType,
                                                                           NumberCast<FuncObjectExpr<F>, FuncObjectExpr<T>> cast) {
        numberCastMap.put(asList(toType, fromType), cast);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ObjectMath<T, FuncObjectExpr<?>, FuncObjectExpr<?>> getObjectMath(Class<T> returnType) {
        return (ObjectMath<T, FuncObjectExpr<?>, FuncObjectExpr<?>>) mathMap.getOrDefault(
                Objects.requireNonNull(returnType, "returnType"), FuncObjectMath.INSTANCE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Number> NumberMath<T, FuncObjectExpr<?>, FuncObjectExpr<?>> getNumberMath(Class<T> returnType) {
        return (NumberMath<T, FuncObjectExpr<?>, FuncObjectExpr<?>>) mathMap.computeIfAbsent(
                Objects.requireNonNull(returnType, "returnType"), aClass -> {
                    throw new UnsupportedOperationException("Unknown Number class " + aClass);
                });
    }

    @SuppressWarnings("unchecked")
    @Override
    public NumberCast<FuncObjectExpr<?>, FuncObjectExpr<?>> getNumberCast(Class<? extends Number> toType,
                                                                          Class<? extends Number> fromType) {
        return (NumberCast<FuncObjectExpr<?>, FuncObjectExpr<?>>) numberCastMap.get(asList(toType, fromType));
    }
}

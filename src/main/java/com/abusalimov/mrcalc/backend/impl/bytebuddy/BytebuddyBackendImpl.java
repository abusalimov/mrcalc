package com.abusalimov.mrcalc.backend.impl.bytebuddy;

import com.abusalimov.mrcalc.backend.Backend;
import com.abusalimov.mrcalc.backend.FunctionAssembler;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.*;

/**
 * The backend implementation TBD.
 *
 * @author Eldar Abusalimov
 */
public class BytebuddyBackendImpl implements Backend<StackStub, Class<?>> {
    private static final Map<Method, Class<?>> functionInterfaceMethodMap = new LinkedHashMap<>();

    static {
        putFunctionInterfaceMethod(BinaryOperator.class, "apply", Object.class, Object.class, Object.class);
        putFunctionInterfaceMethod(LongBinaryOperator.class, "applyAsLong", Long.TYPE, Long.TYPE, Long.TYPE);
        putFunctionInterfaceMethod(DoubleBinaryOperator.class, "applyAsDouble", Double.TYPE, Double.TYPE, Double.TYPE);

        putFunctionInterfaceMethod(Function.class, "apply", Object.class, Object.class);
        putFunctionInterfaceMethod(LongFunction.class, "apply", Object.class, Long.TYPE);
        putFunctionInterfaceMethod(DoubleFunction.class, "apply", Object.class, Double.TYPE);

        putFunctionInterfaceMethod(ToLongFunction.class, "applyAsLong", Long.TYPE, Object.class);
        putFunctionInterfaceMethod(LongUnaryOperator.class, "applyAsLong", Long.TYPE, Long.TYPE);
        putFunctionInterfaceMethod(DoubleToLongFunction.class, "applyAsLong", Long.TYPE, Double.TYPE);

        putFunctionInterfaceMethod(ToDoubleFunction.class, "applyAsDouble", Double.TYPE, Object.class);
        putFunctionInterfaceMethod(LongToDoubleFunction.class, "applyAsDouble", Double.TYPE, Long.TYPE);
        putFunctionInterfaceMethod(DoubleUnaryOperator.class, "applyAsDouble", Double.TYPE, Double.TYPE);
    }

    private static void putFunctionInterfaceMethod(Class<?> cls, String name,
                                                   Class<?> returnType, Class<?>... parameterTypes) {
        try {
            functionInterfaceMethodMap.put(cls.getMethod(name, parameterTypes), cls);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could locate standard methods", e);
        }
    }

    private static Method matchFunctionInterfaceMethod(Class<?> returnType, Class<?>... parameterTypes) {
        outer:
        for (Method method : functionInterfaceMethodMap.keySet()) {
            if (!method.getReturnType().isAssignableFrom(returnType)) {
                continue;
            }
            Class<?>[] methodParameterTypes = method.getParameterTypes();
            if (methodParameterTypes.length != parameterTypes.length) {
                continue;
            }
            for (int i = 0; i < parameterTypes.length; i++) {
                if (!parameterTypes[i].isAssignableFrom(methodParameterTypes[i])) {
                    continue outer;
                }
            }
            return method;
        }

        return null;
    }

    @Override
    public <R> FunctionAssembler<R, StackStub, Class<?>> createFunctionAssembler(Class<R> returnType,
                                                                                        Class<?>... parameterTypes) {
        Method method = matchFunctionInterfaceMethod(returnType, parameterTypes);
//        if (method != null) {
//            return new BytebuddyFunctionAssembler.ForInterface<>(functionInterfaceMethodMap.get(method), method);
//        } else {
            return new BytebuddyFunctionAssembler<>(returnType, parameterTypes);
//        }
    }
}

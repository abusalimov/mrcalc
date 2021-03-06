package com.abusalimov.mrcalc.backend.impl.bytebuddy;

import com.abusalimov.mrcalc.backend.Backend;
import com.abusalimov.mrcalc.backend.FunctionAssembler;
import net.bytebuddy.dynamic.DynamicType;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.*;

/**
 * The backend implementation that generates JVM bytecode using the ByteBuddy framework.
 *
 * @author Eldar Abusalimov
 */
public class BytebuddyBackendImpl implements Backend<StackStub, DynamicType.Unloaded<RuntimeFunction>> {
    private static final Map<Method, Class<?>> functionInterfaceMethodMap = new LinkedHashMap<>();

    static {
        putFunctionInterfaceMethod(BinaryOperator.class, "apply", Object.class, Object.class, Object.class);
        putFunctionInterfaceMethod(LongBinaryOperator.class, "applyAsLong", long.class, long.class, long.class);
        putFunctionInterfaceMethod(DoubleBinaryOperator.class, "applyAsDouble", double.class, double.class,
                double.class);

        putFunctionInterfaceMethod(Function.class, "apply", Object.class, Object.class);
        putFunctionInterfaceMethod(LongFunction.class, "apply", Object.class, long.class);
        putFunctionInterfaceMethod(DoubleFunction.class, "apply", Object.class, double.class);

        putFunctionInterfaceMethod(ToLongFunction.class, "applyAsLong", long.class, Object.class);
        putFunctionInterfaceMethod(LongUnaryOperator.class, "applyAsLong", long.class, long.class);
        putFunctionInterfaceMethod(DoubleToLongFunction.class, "applyAsLong", long.class, double.class);

        putFunctionInterfaceMethod(ToDoubleFunction.class, "applyAsDouble", double.class, Object.class);
        putFunctionInterfaceMethod(LongToDoubleFunction.class, "applyAsDouble", double.class, long.class);
        putFunctionInterfaceMethod(DoubleUnaryOperator.class, "applyAsDouble", double.class, double.class);
    }

    private static void putFunctionInterfaceMethod(Class<?> cls, String name,
                                                   Class<?> returnType, Class<?>... parameterTypes) {
        try {
            functionInterfaceMethodMap.put(cls.getMethod(name, parameterTypes), cls);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could locate standard methods", e);
        }
    }

    private static boolean isJavaAssignable(Class<?> to, Class<?> from) {
        /*
         * Refer to the JVM specification paragraph 4.10.1.2 Verification Type System:
         *
         *     For assignments, interfaces are treated like Object.
         *     ...
         *     Array types are subtypes of Object.
         *     The intent is also that array types are subtypes of Cloneable and java.io.Serializable.
         */
        if (to.isInterface() && !from.isArray()) {
            to = Object.class;
        }
        return to.isAssignableFrom(from);
    }

    private static Method matchFunctionInterfaceMethod(Class<?> returnType, Class<?>... parameterTypes) {
        outer:
        for (Method method : functionInterfaceMethodMap.keySet()) {
            if (!isJavaAssignable(method.getReturnType(), returnType)) {
                continue;
            }
            Class<?>[] methodParameterTypes = method.getParameterTypes();
            if (methodParameterTypes.length != parameterTypes.length) {
                continue;
            }
            for (int i = 0; i < parameterTypes.length; i++) {
                if (!isJavaAssignable(parameterTypes[i], methodParameterTypes[i])) {
                    continue outer;
                }
            }
            return method;
        }

        return null;
    }

    @Override
    public <R> FunctionAssembler<R, StackStub, DynamicType.Unloaded<RuntimeFunction>> createFunctionAssembler(
            Class<R> returnType, Class<?>... parameterTypes) {
        Method method = matchFunctionInterfaceMethod(returnType, parameterTypes);
        if (method != null) {
            return new BytebuddyFunctionAssembler.ForInterface<>(returnType, parameterTypes,
                    functionInterfaceMethodMap.get(method), method);
        } else {
            return new BytebuddyFunctionAssembler<>(returnType, parameterTypes);
        }
    }
}

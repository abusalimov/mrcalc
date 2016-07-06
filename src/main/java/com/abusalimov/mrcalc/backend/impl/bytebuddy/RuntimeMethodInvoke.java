package com.abusalimov.mrcalc.backend.impl.bytebuddy;

import com.abusalimov.mrcalc.runtime.Runtime;
import net.bytebuddy.implementation.bytecode.StackManipulation;

import java.lang.reflect.Method;

/**
 * Helper class for constructing method invocations.
 *
 * @author Eldar Abusalimov
 */
public class RuntimeMethodInvoke {
    private final Method runtimeMethod;

    /**
     * Creates an invoker for the specified method.
     *
     * @param runtimeMethod the method to invoke
     */
    public RuntimeMethodInvoke(Method runtimeMethod) {
        this.runtimeMethod = runtimeMethod;
    }

    /**
     * Creates an invoker for the {@link Runtime} method specified by its name and signature.
     *
     * @param runtimeMethodName the name of the method of the {@link Runtime} class
     * @param parameterTypes    the array of formal parameters accepted by the method
     * @throws IllegalArgumentException in case of a method lookup error
     */
    public RuntimeMethodInvoke(String runtimeMethodName, Class<?>... parameterTypes) {
        this(lookupMethod(Runtime.class, runtimeMethodName, parameterTypes));
    }

    protected static Method lookupMethod(Class<Runtime> runtimeClass, String methodName, Class<?>... parameterTypes) {
        try {
            return runtimeClass.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(String.format("No Runtime method '%s'", methodName), e);
        }
    }

    /**
     * Creates a {@link StackStub} implementing an invocation of the target method with the given arguments.
     *
     * @param arguments the actual arguments to pass to the target method
     * @return a {@link StackStub} invoking the method with the arguments
     */
    public StackStub invokeWithArguments(StackManipulation... arguments) {
        return MethodCallStub.invokeRuntime(runtimeMethod, arguments);
    }
}

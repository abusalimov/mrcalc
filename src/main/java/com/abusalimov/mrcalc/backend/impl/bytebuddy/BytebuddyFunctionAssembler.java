package com.abusalimov.mrcalc.backend.impl.bytebuddy;

import com.abusalimov.mrcalc.backend.*;
import com.abusalimov.mrcalc.runtime.Evaluable;
import com.abusalimov.mrcalc.runtime.Runtime;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.Opcodes;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * @author Eldar Abusalimov
 */
public class BytebuddyFunctionAssembler<R> implements FunctionAssembler<R, StackStub, Class<?>> {
    private static final Map<Class<?>, NumberMath<? extends Number, StackStub>> numberMathMap = new HashMap<>();

    static {
        numberMathMap.put(Long.TYPE, BytebuddyLongMath.INSTANCE);
//        numberMathMap.put(Double.TYPE, DoubleFuncNumberMath.INSTANCE);
    }

    private final Class<R> returnType;
    private final Class<?>[] parameterTypes;

    public BytebuddyFunctionAssembler(Class<R> returnType, Class<?>[] parameterTypes) {
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

    protected DynamicType.Builder.MethodDefinition.ImplementationDefinition<?> getImplementationDefinition() {
        return new ByteBuddy()
//                .with(ClassFileVersion.JAVA_V6)
                .subclass(BaseFunction.class)
                .defineMethod("applyExpr", returnType, Opcodes.ACC_PUBLIC)
                .withParameters((Type[]) parameterTypes);
    }

    @Override
    public Class<?> assemble(StackStub expr) {

        DynamicType.Unloaded<?> dynamicType = getImplementationDefinition()
                .intercept(new Implementation.Simple(new StackStub.Appender(expr, getMethodReturn())))
                .make();
        try {
            Map<TypeDescription, File> typeDescriptionFileMap = dynamicType
                    .saveIn(new File("/home/eldar/tmp/bytebuddy"));
            System.out.println(typeDescriptionFileMap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dynamicType
                .load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();
    }

    protected StackStub getMethodReturn() {
        return instrumentedMethod ->
                MethodReturn.returning(instrumentedMethod.getReturnType().asErasure());
    }

    @Override
    public StackStub lambda(Class<?> function) {
        throw new RuntimeException("NIY lambda");
    }

    @Override
    public Evaluable<R> toEvaluable(Class<?> function) {
        MethodDescription evalMethod = new TypeDescription.ForLoadedType(function)
                .getDeclaredMethods().filter(nameStartsWith("apply")).getOnly();

        Constructor<?> constructor;
        try {
            constructor = function.getConstructor(Runtime.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        MethodCall exprConstructorCall = ArgumentUnpackingMethodCall.construct(constructor).withArgument(0);

        DynamicType.Unloaded<Evaluable> dynamicType = new ByteBuddy()
                .subclass(Evaluable.class)
                .method(named("eval"))
                .intercept(new Implementation.Compound(exprConstructorCall,
                        new ArgumentUnpackingMethodCall(new MethodCall.MethodLocator.ForExplicitMethod(evalMethod))
                                .withArgumentsArray(1, evalMethod.getParameters().size())
                                .withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC)))
                .make();
        Class<? extends Evaluable> evaluableClass = dynamicType
                .load(function.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();
        try {
            return (Evaluable<R>) evaluableClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public ArgumentLoad<StackStub> getArgumentLoad(Class<?> parameterType) {
        return slot -> instrumentedMethod -> {
            ParameterDescription parameterDescription = instrumentedMethod.getParameters().get(slot);
            return MethodVariableAccess.of(parameterDescription.getType()).loadOffset(parameterDescription.getOffset());
        };
    }

    @Override
    public <T extends Number> NumberMath<T, StackStub> getNumberMath(Class<T> returnType) {
        @SuppressWarnings("unchecked") Map<Class<T>, NumberMath<T, StackStub>> mathMap = (Map) numberMathMap;
        return mathMap.computeIfAbsent(
                Objects.requireNonNull(returnType, "returnType"), aClass -> {
                    throw new UnsupportedOperationException("Unknown Number class " + aClass);
                });
    }

    @Override
    public NumberCast<StackStub, StackStub> getNumberCast(Class<? extends Number> toType,
                                                          Class<? extends Number> fromType) {
        StackManipulation assignStackManipulation = Assigner.DEFAULT
                .assign(new TypeDescription.ForLoadedType(fromType).asGenericType(),
                        new TypeDescription.ForLoadedType(toType).asGenericType(), Assigner.Typing.STATIC);
        return expr -> new StackStub.Compound(expr, new StackStub.Simple(assignStackManipulation));
    }

    @Override
    public SequenceRange<StackStub, StackStub> getSequenceRange(Class<? extends Number> elementType) {
        throw new RuntimeException("NIY getSequenceRange");
    }

    @Override
    public SequenceReduce<StackStub, StackStub, StackStub> getSequenceReduce(Class<?> returnType) {
        throw new RuntimeException("NIY getSequenceReduce");
    }

    @Override
    public SequenceMap<StackStub, StackStub, StackStub> getSequenceMap(Class<?> returnElementType,
                                                                       Class<?> elementType) {
        throw new RuntimeException("NIY getSequenceMap");
    }

    public static class ForInterface<R> extends BytebuddyFunctionAssembler<R> {
        private final Class<?> methodInterface;
        private final Method method;

        public ForInterface(Class<?> methodInterface, Method method) {
            //noinspection unchecked
            super((Class<R>) method.getReturnType(), method.getParameterTypes());

            this.methodInterface = methodInterface;
            this.method = method;
        }

        @Override
        protected DynamicType.Builder.MethodDefinition.ImplementationDefinition<?> getImplementationDefinition() {
            return new ByteBuddy()
                    .subclass(BaseFunction.class)
                    .implement(methodInterface)
                    .method(is(method));
        }
    }

    public static abstract class BaseFunction {
        private final Runtime runtime;

        public BaseFunction(Runtime runtime) {
            this.runtime = runtime;
        }

        public Runtime getRuntime() {
            return runtime;
        }
    }
}

package com.abusalimov.mrcalc.backend.impl.bytebuddy;

import com.abusalimov.mrcalc.backend.*;
import com.abusalimov.mrcalc.runtime.Evaluable;
import com.abusalimov.mrcalc.runtime.Runtime;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.implementation.bytecode.member.FieldAccess;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * @author Eldar Abusalimov
 */
public class BytebuddyFunctionAssembler<R> implements FunctionAssembler<R, StackStub,
        DynamicType.Unloaded<BytebuddyFunctionAssembler.BaseFunction>> {
    private final Class<R> returnType;
    private final Class<?>[] parameterTypes;
    private final List<DynamicType.Unloaded<BaseFunction>> lambdas = new ArrayList<>();

    public BytebuddyFunctionAssembler(Class<R> returnType, Class<?>[] parameterTypes) {
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

    protected DynamicType.Builder.MethodDefinition.ImplementationDefinition<BaseFunction> getDynamicBuilder() {
        List<TypeDescription> lambdaTypeDescriptions = lambdas.stream()
                .map(DynamicType::getTypeDescription)
                .collect(Collectors.toList());

        DynamicType.Builder<BaseFunction> builder = new ByteBuddy()
                .subclass(BaseFunction.class);

        for (TypeDescription lambdaTypeDescription : lambdaTypeDescriptions) {
            builder = builder.defineField(
                    getLambdaName(lambdaTypeDescription),
                    lambdaTypeDescription,
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC);
        }

        List<StackStub> lambdaFieldInitializers = lambdaTypeDescriptions.stream()
                .map(this::createLambdaInitializer)
                .collect(Collectors.toList());
        Implementation.Compound constructorImplementation = new Implementation.Compound(
                lambdaFieldInitializers.toArray(new Implementation[0]));

        return builder
                .constructor(takesArguments(Runtime.class))
                .intercept(SuperMethodCall.INSTANCE
                        .andThen(new Implementation.Compound(constructorImplementation,
                                new Implementation.Simple(MethodReturn.VOID))))
                .defineMethod("applyExpr", returnType, Opcodes.ACC_PUBLIC)
                .withParameters((Type[]) parameterTypes);
    }

    protected DynamicType.Builder.MethodDefinition.ImplementationDefinition<BaseFunction> getImplementationDefinition() {
        return getDynamicBuilder();
    }

    private StackStub createLambdaInitializer(TypeDescription typeDescription) {
        return new StackStub.Compound(
                new StackStub.Simple(MethodVariableAccess.REFERENCE.loadOffset(0)),  // this

                MethodCallStub.constructWithRuntimeArgument(typeDescription),

                (implementationTarget, instrumentedMethod) -> {
                    TypeDescription instrumentedType = implementationTarget.getInstrumentedType();
                    FieldDescription fieldDescription = getLambdaFieldDescription(instrumentedType, typeDescription);

                    return FieldAccess.forField(fieldDescription).putter();
                });
    }

    @Override
    public DynamicType.Unloaded<BaseFunction> assemble(StackStub expr) {
        DynamicType.Unloaded<BaseFunction> dynamicType = getImplementationDefinition()
                .intercept(new StackStub.Compound(expr, getMethodReturn()))
                .make();

        return dynamicType.include(lambdas);
    }

    protected StackStub getMethodReturn() {
        return (StackStub.ForMethod) instrumentedMethod ->
                MethodReturn.returning(instrumentedMethod.getReturnType().asErasure());
    }

    private String getLambdaName(TypeDescription typeDescription) {
        String typeName = typeDescription.getName();
        int dollarIndex = typeName.lastIndexOf('$');
        if (dollarIndex != -1) {
            typeName = typeName.substring(dollarIndex);
        }
        return "lambda" + typeName;
    }

    @Override
    public StackStub lambda(DynamicType.Unloaded<BaseFunction> function) {
        lambdas.add(function);
        TypeDescription typeDescription = function.getTypeDescription();

        return (implementationTarget, instrumentedMethod) -> {
            TypeDescription instrumentedType = implementationTarget.getInstrumentedType();
            FieldDescription fieldDescription = getLambdaFieldDescription(instrumentedType, typeDescription);

            return new StackManipulation.Compound(
                    MethodVariableAccess.REFERENCE.loadOffset(0),  // this
                    FieldAccess.forField(fieldDescription).getter());
        };
    }

    protected FieldDescription getLambdaFieldDescription(TypeDescription instrumentedType,
                                                         TypeDescription lambdaTypeDescription) {
        return instrumentedType.getDeclaredFields().filter(named(getLambdaName(lambdaTypeDescription))).getOnly();
    }

    @Override
    public Evaluable<R> toEvaluable(DynamicType.Unloaded<BaseFunction> function) {
        MethodDescription evalMethod = function.getTypeDescription()
                .getDeclaredMethods().filter(ElementMatchers.named("applyExpr")).getOnly();

        StackStub constructorCall = MethodCallStub.constructWithRuntimeArgument(function.getTypeDescription());

        DynamicType.Unloaded<Evaluable> dynamicType = new ByteBuddy()
                .subclass(Evaluable.class)
                .method(named("eval"))
                .intercept(new StackStub.Compound(constructorCall,
                        new MethodCallStub(evalMethod)
                                .withArgumentsArray(1, evalMethod.getParameters().size())))
                .make();

        Class<? extends Evaluable> evaluableClass = dynamicType.include(function)
                .load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();
        try {
            return (Evaluable<R>) evaluableClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ArgumentLoad<StackStub> getArgumentLoad(Class<?> parameterType) {
        return slot -> (StackStub.ForMethod) instrumentedMethod -> {
            ParameterDescription parameterDescription = instrumentedMethod.getParameters().get(slot);
            return new StackManipulation.Compound(
                    MethodVariableAccess.of(parameterDescription.getType())
                            .loadOffset(parameterDescription.getOffset()),
                    Assigner.DEFAULT.assign(parameterDescription.getType(),
                            new TypeDescription.ForLoadedType(parameterType).asGenericType(),
                            Assigner.Typing.DYNAMIC));
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Number> NumberMath<T, StackStub> getNumberMath(Class<T> returnType) {
        return (NumberMath<T, StackStub>) BytebuddyNumberMath.forType(returnType);
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
        return BytebuddySequenceRange.forType(elementType);
    }

    @Override
    public SequenceReduce<StackStub, StackStub, StackStub> getSequenceReduce(Class<?> returnType) {
        return BytebuddySequenceReduce.forType(returnType);
    }

    @Override
    public SequenceMap<StackStub, StackStub, StackStub> getSequenceMap(Class<?> returnElementType,
                                                                       Class<?> elementType) {
        return BytebuddySequenceMap.forType(returnElementType, elementType);
    }

    public static class ForInterface<R> extends BytebuddyFunctionAssembler<R> {
        private final Class<?> methodInterface;
        private final Method method;

        public ForInterface(Class<R> returnType, Class<?>[] parameterTypes, Class<?> methodInterface, Method method) {
            super(returnType, parameterTypes);

            this.methodInterface = methodInterface;
            this.method = method;
        }

        @Override
        protected DynamicType.Builder.MethodDefinition.ImplementationDefinition<BaseFunction> getImplementationDefinition() {
            return getDynamicBuilder()
                    .intercept(MethodCall.invoke(method).withAllArguments()
                            .withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC))
                    .implement(methodInterface)
                    .method(is(method));
        }
    }

    public static abstract class BaseFunction {
        public final Runtime runtime;

        public BaseFunction(Runtime runtime) {
            this.runtime = runtime;
        }
    }

}

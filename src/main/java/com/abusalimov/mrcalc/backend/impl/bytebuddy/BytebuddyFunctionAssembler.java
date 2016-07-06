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
 * The function assembler that generates function classes dynamically using the ByteBuddy framework.
 * <p>
 * Each {@link #assemble(StackStub) assembled} function class extend a special {@link RuntimeFunction} class and has a
 * single {@link RuntimeFunction#RuntimeFunction(Runtime) constructor} accepting an instance of {@link Runtime}. The
 * constructor is responsible for initializing lambda functions referenced from the function, if any. Each {@link
 * #lambda(DynamicType.Unloaded) lambda} function, in turn, is an appropriately {@link ForInterface#assemble(StackStub)
 * assembled} class extending the very same {@link RuntimeFunction base} class and additionally implementing the proper
 * {@link FunctionalInterface} accepted by the methods of the {@link Runtime} class.
 * <p>
 * The resulting {@link Evaluable} {@link #toEvaluable(DynamicType.Unloaded) constructed} using the function assembler
 * takes the Runtime instance passed in as the first argument to the {@link Evaluable#eval(Runtime, Object...)} method
 * and instantiates the appropriate function class providing it with the instance of Runtime. The function class
 * instantiates the required lambda functions, which in turn instantiate their lambdas, if any, and so on. That is, upon
 * invoking the actual function the whole tree of lambda function used across the expression is fully initialized.
 * <p>
 * Example:
 * <pre>{@code
 *     var id = 0
 *     var seq = {0, 9}
 *     print reduce(seq, id, a b -> a + b)
 * }</pre>
 * <p>
 * The expression of the last statements gets compiled into roughly the following:
 * <pre><code>
 *     // Binds the packed variables to the expression arguments
 *     public class EvaluableImpl implements Evaluable {
 *         {@literal @Override}
 *         public Object eval(Runtime runtime, Object... args) {
 *             FunctionImpl$0 func = new FunctionImpl$0(runtime);  // instantiate the top-level function object
 *
 *             // unpack arguments from the variables context
 *             Sequence.OfLong seq = (Sequence.OfLong) args[0];
 *             long id = ((Long) args[1]).longValue();
 *
 *             long result = func.applyExpr(seq, id);  // evaluate the expression
 *
 *             return Long.valueOf(result);
 *         }
 *     }
 *
 *     // Implements {@literal seq id -> reduce(seq, id, a b -> a + b) }
 *     public class FunctionImpl$0 extends RuntimeFunction {
 *         private final FunctionImpl$1 lambda$1;
 *
 *         public FunctionImpl$0(Runtime runtime) {
 *             super(runtime);
 *             this.lambda$1 = new FunctionImpl$1(runtime);
 *         }
 *
 *         public long applyExpr(Sequence.OfLong seq, long id) {
 *             return this.runtime.reduceLong(seq, id, this.lambda$1);
 *         }
 *     }
 *
 *     // Implements {@literal a b -> a + b }
 *     public class FunctionImpl$1 extends RuntimeFunction implements LongBinaryOperator {
 *         public FunctionImpl$1(Runtime runtime) {
 *             super(runtime);
 *         }
 *
 *         {@literal @Override}
 *         public long applyAsLong(long left, long right) {
 *             return left + right;
 *         }
 *     }
 * </code></pre>
 *
 * @param <R> the return type of the function constructed using this assembler, for additional type check
 * @author Eldar Abusalimov
 */
public class BytebuddyFunctionAssembler<R> implements FunctionAssembler<R, StackStub, DynamicType.Unloaded<RuntimeFunction>> {
    private final Class<R> returnType;
    private final Class<?>[] parameterTypes;
    private final List<DynamicType.Unloaded<RuntimeFunction>> lambdas = new ArrayList<>();

    public BytebuddyFunctionAssembler(Class<R> returnType, Class<?>[] parameterTypes) {
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

    protected DynamicType.Builder.MethodDefinition.ImplementationDefinition<RuntimeFunction> getDynamicBuilder() {
        List<TypeDescription> lambdaTypeDescriptions = lambdas.stream()
                .map(DynamicType::getTypeDescription)
                .collect(Collectors.toList());

        DynamicType.Builder<RuntimeFunction> builder = new ByteBuddy()
                .subclass(RuntimeFunction.class);

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
    public DynamicType.Unloaded<RuntimeFunction> assemble(StackStub expr) {
        DynamicType.Unloaded<RuntimeFunction> dynamicType = getDynamicBuilder()
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
    public StackStub lambda(DynamicType.Unloaded<RuntimeFunction> function) {
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
    public Evaluable<R> toEvaluable(DynamicType.Unloaded<RuntimeFunction> function) {
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
        protected DynamicType.Builder.MethodDefinition.ImplementationDefinition<RuntimeFunction> getDynamicBuilder() {
            /*
             * Makes the "applyExpr" method simply delegate to the method implementing the interface.
             * The former is likely not used anyway.
             */
            return super.getDynamicBuilder()
                    .intercept(MethodCall.invoke(method).withAllArguments()
                            .withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC))
                    .implement(methodInterface)
                    .method(is(method));
        }
    }

}

package com.abusalimov.mrcalc.backend.impl.bytebuddy;

import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.method.ParameterList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.StackSize;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.implementation.bytecode.collection.ArrayAccess;
import net.bytebuddy.implementation.bytecode.constant.IntegerConstant;
import net.bytebuddy.implementation.bytecode.member.FieldAccess;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.utility.CompoundList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Extends the {@link MethodCall} class with few necessary methods and constructors.
 *
 * @author Eldar Abusalimov
 */
public class RawMethodCall extends MethodCall implements StackStub {

    private static final Field RUNTIME_FIELD;

    static {
        try {
            RUNTIME_FIELD = BytebuddyFunctionAssembler.BaseFunction.class.getField("runtime");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new method call without a specified target.
     *
     * @param method The method to use.
     */
    protected RawMethodCall(Method method) {
        this(new MethodDescription.ForLoadedMethod(method),
                TargetHandler.ForStackTopOperand.INSTANCE,
                Collections.emptyList(),
                MethodInvoker.ForVirtualInvocation.WithImplicitType.INSTANCE,
                TerminationHandler.ForMethodReturn.INSTANCE,
                Assigner.DEFAULT,
                Assigner.Typing.STATIC);
    }

    /**
     * Creates a new method call without a specified target.
     *
     * @param methodDescription The method description to use.
     */
    protected RawMethodCall(MethodDescription methodDescription) {
        this(new MethodLocator.ForExplicitMethod(methodDescription),
                TargetHandler.ForStackTopOperand.INSTANCE,
                Collections.emptyList(),
                MethodInvoker.ForVirtualInvocation.WithImplicitType.INSTANCE,
                TerminationHandler.ForMethodReturn.INSTANCE,
                Assigner.DEFAULT,
                Assigner.Typing.STATIC);
    }

    /**
     * Creates a new method call without a specified target.
     *
     * @param methodLocator The method locator to use.
     */
    protected RawMethodCall(MethodLocator methodLocator) {
        this(methodLocator,
                TargetHandler.ForStackTopOperand.INSTANCE,
                Collections.emptyList(),
                MethodInvoker.ForVirtualInvocation.WithImplicitType.INSTANCE,
                TerminationHandler.ForMethodReturn.INSTANCE,
                Assigner.DEFAULT,
                Assigner.Typing.STATIC);
    }

    /**
     * Creates a new method call without a specified target.
     *
     * @param method             The method to use.
     * @param targetHandler      The target handler to use.
     * @param argumentLoaders    The argument loader to load arguments onto the operand stack in their application
     *                           order.
     * @param methodInvoker      The method invoker to use.
     * @param terminationHandler The termination handler to use.
     * @param assigner           The assigner to use.
     * @param typing             Indicates if dynamic type castings should be attempted for incompatible assignments.
     */
    protected RawMethodCall(Method method, MethodCall.TargetHandler targetHandler,
                            List<MethodCall.ArgumentLoader.Factory> argumentLoaders,
                            MethodInvoker methodInvoker,
                            MethodCall.TerminationHandler terminationHandler,
                            Assigner assigner,
                            Assigner.Typing typing) {
        this(new MethodDescription.ForLoadedMethod(method),
                targetHandler, argumentLoaders, methodInvoker, terminationHandler, assigner, typing);
    }

    /**
     * Creates a new method call without a specified target.
     *
     * @param methodDescription  The method description to use.
     * @param targetHandler      The target handler to use.
     * @param argumentLoaders    The argument loader to load arguments onto the operand stack in their application
     *                           order.
     * @param methodInvoker      The method invoker to use.
     * @param terminationHandler The termination handler to use.
     * @param assigner           The assigner to use.
     * @param typing             Indicates if dynamic type castings should be attempted for incompatible assignments.
     */
    protected RawMethodCall(MethodDescription methodDescription, MethodCall.TargetHandler targetHandler,
                            List<MethodCall.ArgumentLoader.Factory> argumentLoaders,
                            MethodInvoker methodInvoker,
                            MethodCall.TerminationHandler terminationHandler,
                            Assigner assigner,
                            Assigner.Typing typing) {
        this(new MethodLocator.ForExplicitMethod(methodDescription),
                targetHandler, argumentLoaders, methodInvoker, terminationHandler, assigner, typing);
    }

    /**
     * Creates a new method call implementation.
     *
     * @param methodLocator      The method locator to use.
     * @param targetHandler      The target handler to use.
     * @param argumentLoaders    The argument loader to load arguments onto the operand stack in their application
     *                           order.
     * @param methodInvoker      The method invoker to use.
     * @param terminationHandler The termination handler to use.
     * @param assigner           The assigner to use.
     * @param typing             Indicates if dynamic type castings should be attempted for incompatible assignments.
     */
    protected RawMethodCall(MethodLocator methodLocator, MethodCall.TargetHandler targetHandler,
                            List<MethodCall.ArgumentLoader.Factory> argumentLoaders,
                            MethodInvoker methodInvoker,
                            MethodCall.TerminationHandler terminationHandler,
                            Assigner assigner,
                            Assigner.Typing typing) {
        super(methodLocator, targetHandler, argumentLoaders, methodInvoker, terminationHandler, assigner, typing);
    }

    /**
     * Invokes the given constructor in order to create an instance.
     *
     * @param constructor The constructor to invoke.
     * @return A method call that invokes the given constructor without providing any arguments.
     */
    public static MethodCall construct(Constructor<?> constructor) {
        return construct(new MethodDescription.ForLoadedConstructor(constructor));
    }

    /**
     * Invokes the given constructor in order to create an instance.
     *
     * @param methodDescription A description of the constructor to invoke.
     * @return A method call that invokes the given constructor without providing any arguments.
     */
    public static MethodCall construct(MethodDescription methodDescription) {
        if (!methodDescription.isConstructor()) {
            throw new IllegalArgumentException("Not a constructor: " + methodDescription);
        }
        return new RawMethodCall(new MethodLocator.ForExplicitMethod(methodDescription),
                MethodCall.TargetHandler.ForConstructingInvocation.INSTANCE,
                Collections.emptyList(),
                MethodInvoker.ForContextualInvocation.INSTANCE,
                TerminationHandler.ForStub.INSTANCE,
                Assigner.DEFAULT,
                Assigner.Typing.STATIC);
    }

    public static RawMethodCall invokeRuntime(Method method, StackManipulation... arguments) {
        return new RawMethodCall(method,
                new TargetHandler.ForSuperInstanceField(new FieldDescription.ForLoadedField(RUNTIME_FIELD)),
                Collections.singletonList(new ArgumentLoader.ForStackManipulations(arguments)),
                MethodInvoker.ForVirtualInvocation.WithImplicitType.INSTANCE,
                TerminationHandler.ForStub.INSTANCE,
                Assigner.DEFAULT,
                Assigner.Typing.STATIC);
    }

//    public static RawMethodCall invokeRuntime(Method method, int arguments) {
//        return new RawMethodCall(method,
//                new MethodCall.TargetHandler.ForInstanceField("runtime", TypeDefinition.Sort.describe(Runtime.class)),
//                Collections.singletonList(new ArgumentLoader.ForStackTopOperand(arguments)),
//                MethodInvoker.ForVirtualInvocation.WithImplicitType.INSTANCE,
//                TerminationHandler.ForStub.INSTANCE,
//                Assigner.DEFAULT,
//                Assigner.Typing.STATIC);
//    }

    /**
     * Defines a method call that unpacks the array found in the specified parameter of the instrumented method and
     * passes its elements as arguments to the invoked method.
     * <p>
     * <pre>
     *     Foo instrumentedMethod(Bar[] args) {
     *         return invokedMethod(args[0], args[1], ...);
     *     }
     * </pre>
     *
     * @param parameterIndex       the index of the parameter containing the array of argument values
     * @param argumentsArrayLength how many arguments to unpack from the array
     * @return a new {@link MethodCall} instance
     */
    public RawMethodCall withArgumentsArray(int parameterIndex, int argumentsArrayLength) {
        if (parameterIndex < 0) {
            throw new IllegalArgumentException("Negative parameter index: " + parameterIndex);
        }
        if (argumentsArrayLength < 0) {
            throw new IllegalArgumentException("Negative arguments array length: " + argumentsArrayLength);
        }

        ArgumentLoader.Factory argumentLoader = new ArgumentLoader.ForArgumentsArray.FromInstrumentedMethodArgument(
                parameterIndex, argumentsArrayLength);

        return new RawMethodCall(methodLocator,
                targetHandler,
                CompoundList.of(this.argumentLoaders, argumentLoader),
                methodInvoker,
                terminationHandler,
                assigner,
                typing);
    }

    @Override
    public ByteCodeAppender appender(Target implementationTarget) {
        return new StackStub.Appender(implementationTarget, this);
    }

    @Override
    public StackManipulation eval(Target implementationTarget, MethodDescription instrumentedMethod) {
        MethodDescription invokedMethod = methodLocator.resolve(instrumentedMethod);
        List<MethodCall.ArgumentLoader> argumentLoaders = new ArrayList<>(RawMethodCall.this.argumentLoaders.size());
        for (MethodCall.ArgumentLoader.Factory argumentLoader : RawMethodCall.this.argumentLoaders) {
            argumentLoaders.addAll(argumentLoader.make(implementationTarget.getInstrumentedType(), instrumentedMethod));
        }
        ParameterList<?> parameters = invokedMethod.getParameters();
        Iterator<? extends ParameterDescription> parameterIterator = parameters.iterator();
        if (parameters.size() != argumentLoaders.size()) {
            throw new IllegalStateException(invokedMethod + " does not take " + argumentLoaders.size() + " arguments");
        }
        List<StackManipulation> argumentInstructions = argumentLoaders.stream()
                .map(argumentLoader -> argumentLoader.resolve(parameterIterator.next(), assigner, typing))
                .collect(Collectors.toList());
        return new StackManipulation.Compound(
                targetHandler.resolve(invokedMethod, instrumentedMethod, implementationTarget.getInstrumentedType(),
                        assigner, typing),
                new StackManipulation.Compound(argumentInstructions),
                methodInvoker.invoke(invokedMethod, implementationTarget),
                terminationHandler.resolve(invokedMethod, instrumentedMethod, assigner, typing)
        );
    }

    /**
     * Extends the {@link MethodCall.ArgumentLoader} to get access to the protected members.
     */
    protected interface ArgumentLoader extends MethodCall.ArgumentLoader {

        /**
         * Provides the default no-op {@link MethodCall.ArgumentLoader.Factory#prepare(InstrumentedType)} method.
         */
        interface Factory extends MethodCall.ArgumentLoader.Factory {
            @Override
            default InstrumentedType prepare(InstrumentedType instrumentedType) {
                return instrumentedType;
            }
        }

        /**
         * Produces no-op ArgumentLoaders assuming that the necessary arguments are already on stack.
         */
        class ForStackManipulations implements ArgumentLoader.Factory {
            private final List<StackManipulation> stackManipulations;

            public ForStackManipulations(StackManipulation... stackManipulations) {
                this(Arrays.asList(stackManipulations));
            }

            public ForStackManipulations(List<StackManipulation> stackManipulations) {
                this.stackManipulations = stackManipulations;
            }

            @Override
            public List<MethodCall.ArgumentLoader> make(TypeDescription instrumentedType,
                                                        MethodDescription instrumentedMethod) {
                return stackManipulations.stream()
                        .map(stackManipulation -> (ArgumentLoader) (target, assigner, typing) -> stackManipulation)
                        .collect(Collectors.toList());
            }
        }

        /**
         * Produces no-op ArgumentLoaders assuming that the necessary arguments are already on stack.
         */
        class ForStackTopOperand implements ArgumentLoader.Factory {
            private final int arguments;

            public ForStackTopOperand() {
                this(1);
            }

            public ForStackTopOperand(int arguments) {
                this.arguments = arguments;
            }

            @Override
            public List<MethodCall.ArgumentLoader> make(TypeDescription instrumentedType,
                                                        MethodDescription instrumentedMethod) {
                ArgumentLoader trivialArgumentLoader = (target, assigner, typing) ->
                        new StackOperandTrivialManipulation(target.getType().getStackSize());
                return Collections.nCopies(arguments, trivialArgumentLoader);
            }
        }

        /**
         * Loads an element of given array found in a parameter of the instrumented method onto the operand stack.
         */
        class ForArgumentsArray implements ArgumentLoader {
            private final StackManipulation arrayLoad;
            private final int index;
            private final TypeDescription.Generic componentType;

            /**
             * Creates an argument loader for a given array element.
             *
             * @param arrayLoad     instructions to use to load the array onto the stack
             * @param index         the parameterIndex of the array element to unpack as an argument
             * @param componentType the array element type
             */
            protected ForArgumentsArray(StackManipulation arrayLoad, int index,
                                        TypeDescription.Generic componentType) {
                this.arrayLoad = arrayLoad;
                this.index = index;
                this.componentType = componentType;
            }

            @Override
            public StackManipulation resolve(ParameterDescription target, Assigner assigner,
                                             Assigner.Typing typing) {
                return new StackManipulation.Compound(
                        arrayLoad,
                        IntegerConstant.forValue(index),
                        ArrayAccess.REFERENCE.load(),
                        assigner.assign(componentType, target.getType(), typing));
            }

            /**
             * A factory for argument loaders that supplies all arguments of the instrumented method as arguments.
             */
            protected static class FromInstrumentedMethodArgument implements ArgumentLoader.Factory {
                /**
                 * The parameterIndex of the parameter to be loaded onto the operand stack.
                 */
                private final int parameterIndex;

                /**
                 * The number of arguments to unpack.
                 */
                private final int argumentsArrayLength;

                /**
                 * Creates a factory for an argument loader that supplies a method parameter as an argument.
                 *
                 * @param parameterIndex       The parameterIndex of the array parameter to supply.
                 * @param argumentsArrayLength The number of arguments to unpack for the array.
                 */
                protected FromInstrumentedMethodArgument(int parameterIndex, int argumentsArrayLength) {
                    this.parameterIndex = parameterIndex;
                    this.argumentsArrayLength = argumentsArrayLength;
                }

                @Override
                public List<MethodCall.ArgumentLoader> make(TypeDescription instrumentedType,
                                                            MethodDescription instrumentedMethod) {
                    ParameterDescription parameterDescription = instrumentedMethod.getParameters().get(parameterIndex);
                    StackManipulation arrayLoad = createArrayLoad(parameterDescription);
                    TypeDescription.Generic componentType = parameterDescription.getType().getComponentType();

                    List<MethodCall.ArgumentLoader> argumentLoaders = new ArrayList<>(argumentsArrayLength);
                    for (int i = 0; i < argumentsArrayLength; i++) {
                        argumentLoaders.add(new ForArgumentsArray(arrayLoad, i, componentType));
                    }
                    return argumentLoaders;
                }

                /**
                 * Creates instructions loading the array from an argument of the enclosing method.
                 *
                 * @param parameterDescription the type of the parameter holding the array
                 * @return the instructions to load the array
                 */
                public StackManipulation createArrayLoad(ParameterDescription parameterDescription) {
                    if (!parameterDescription.getType().isArray()) {
                        throw new IllegalStateException("Parameter " + parameterDescription + " is not an array");
                    }

                    return MethodVariableAccess
                            .of(parameterDescription.getType().asErasure())
                            .loadOffset(parameterDescription.getOffset());
                }
            }
        }
    }

    /**
     * A target handler is responsible for invoking a method for a {@link net.bytebuddy.implementation.MethodCall}.
     */
    protected interface TargetHandler extends MethodCall.TargetHandler {

        enum ForStackTopOperand implements MethodCall.TargetHandler {
            INSTANCE;

            @Override
            public StackManipulation resolve(MethodDescription invokedMethod, MethodDescription instrumentedMethod,
                                             TypeDescription instrumentedType, Assigner assigner,
                                             Assigner.Typing typing) {
                return new StackOperandTrivialManipulation(Object.class);
            }

            @Override
            public InstrumentedType prepare(InstrumentedType instrumentedType) {
                return instrumentedType;
            }
        }

        /**
         * Creates a target handler that stores the instance to invoke a method on in an instance field.
         */
        class ForSuperInstanceField implements TargetHandler {
            private FieldDescription fieldDescription;

            public ForSuperInstanceField(FieldDescription fieldDescription) {
                this.fieldDescription = fieldDescription;
            }

            @Override
            public StackManipulation resolve(MethodDescription invokedMethod, MethodDescription instrumentedMethod,
                                             TypeDescription instrumentedType, Assigner assigner,
                                             Assigner.Typing typing) {
                return new StackManipulation.Compound(
                        invokedMethod.isStatic()
                                ? StackManipulation.Trivial.INSTANCE
                                : MethodVariableAccess.REFERENCE.loadOffset(0),
                        FieldAccess.forField(fieldDescription).getter());
            }

            @Override
            public InstrumentedType prepare(InstrumentedType instrumentedType) {
                return instrumentedType;
            }

            @Override
            public boolean equals(Object other) {
                return this == other || !(other == null || getClass() != other.getClass()) &&
                                        fieldDescription.equals(((ForSuperInstanceField) other).fieldDescription);
            }

            @Override
            public int hashCode() {
                return fieldDescription.hashCode();
            }

            @Override
            public String toString() {
                return "MethodCall.TargetHandler.ForInstanceField{" +
                       "fieldDescription=" + fieldDescription + '}';
            }
        }

        /**
         * Invokes a method in order to construct a new instance.
         */
        class ForTargetInvocation implements MethodCall.TargetHandler {
            private final StackManipulation target;

            public ForTargetInvocation(StackManipulation target) {
                this.target = target;
            }

            @Override
            public StackManipulation resolve(MethodDescription invokedMethod, MethodDescription instrumentedMethod,
                                             TypeDescription instrumentedType, Assigner assigner,
                                             Assigner.Typing typing) {
                if (invokedMethod.isStatic()) {
                    return StackManipulation.Trivial.INSTANCE;
                }
                return target;
            }

            @Override
            public InstrumentedType prepare(InstrumentedType instrumentedType) {
                return instrumentedType;
            }
        }
    }

    /**
     * A termination handler is responsible to handle the return value of a method that is invoked via a {@link
     * net.bytebuddy.implementation.MethodCall}.
     */
    protected interface TerminationHandler extends MethodCall.TerminationHandler {

        /**
         * Drops the return value of the called method from the operand stack without returning from the intercepted
         * method.
         */
        enum ForStub implements MethodCall.TerminationHandler {
            INSTANCE;

            @Override
            public StackManipulation resolve(MethodDescription invokedMethod, MethodDescription instrumentedMethod,
                                             Assigner assigner, Assigner.Typing typing) {
                return StackManipulation.Trivial.INSTANCE;
            }

        }
    }

    /**
     * No-op stack manipulation that just reports the the specified size.
     */
    protected static class StackOperandTrivialManipulation implements StackManipulation {
        private final int size;

        public StackOperandTrivialManipulation(StackSize stackSize) {
            size = stackSize.getSize();
        }

        public StackOperandTrivialManipulation(Class<?> operandType) {
            this(StackSize.of(operandType));
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public Size apply(MethodVisitor methodVisitor, Context implementationContext) {
            return new Size(0, size);
        }
    }
}

package com.abusalimov.mrcalc.backend.impl.bytebuddy;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.StackManipulation;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Stack stub is an {@link Implementation} that exposes its underlying {@link StackManipulation}. It allows to combine
 * complex expressions into a single expression statement using the exposed stack manipulations and to leverage the
 * power of ByteBuddy in calculating the proper stack impact of the compound expression.
 *
 * @author Eldar Abusalimov
 */
public interface StackStub extends Implementation {
    /**
     * Emits the proper {@link StackManipulation} instructions corresponding to this (sub-)expression implementation.
     *
     * @param implementationTarget the target of the current implementation
     * @param instrumentedMethod   the target method of the instrumentation
     * @return the instructions evaluating this (sub-)expression
     */
    StackManipulation eval(Target implementationTarget, MethodDescription instrumentedMethod);

    /**
     * Replaces the default {@link #eval(Target, MethodDescription)} with the given one.
     *
     * @param evalDelegate the stack stub to delegate the {@link #eval(Target, MethodDescription)} method call to
     * @return a new {@link StackStub} with overridden {@link #eval(Target, MethodDescription)} method
     */
    default StackStub withEval(StackStub evalDelegate) {
        return new WithEval(this, evalDelegate);
    }

    @Override
    default InstrumentedType prepare(InstrumentedType instrumentedType) {
        return instrumentedType;
    }

    @Override
    default ByteCodeAppender appender(Target implementationTarget) {
        return (methodVisitor, implementationContext, instrumentedMethod) -> {
            StackManipulation stackManipulation = eval(implementationTarget, instrumentedMethod);
            StackManipulation.Size stackSize = stackManipulation.apply(methodVisitor, implementationContext);
            return new ByteCodeAppender.Size(stackSize.getMaximalSize(), instrumentedMethod.getStackSize());
        };
    }

    /**
     * SAM type with the overloaded {@link #eval(MethodDescription)} method accepting a single {@link
     * MethodDescription} argument.
     */
    interface ForMethod extends StackStub {
        /**
         * Emits the proper {@link StackManipulation} instructions corresponding to this (sub-)expression
         * implementation.
         *
         * @param instrumentedMethod the target method of the instrumentation
         * @return the instructions evaluating this (sub-)expression
         */
        StackManipulation eval(MethodDescription instrumentedMethod);

        default StackManipulation eval(Target implementationTarget, MethodDescription instrumentedMethod) {
            return eval(instrumentedMethod);
        }
    }

    /**
     * A simple stack stub that only represents a given array of {@link StackManipulation}s.
     */
    class Simple implements StackStub {
        private final StackManipulation stackManipulation;

        /**
         * Creates a new simple stack stub which represents the given stack manipulation.
         *
         * @param stackManipulations the stack manipulations to emit for this stack stub in their application order.
         */
        public Simple(StackManipulation... stackManipulations) {
            this(Arrays.asList(stackManipulations));
        }

        /**
         * Creates a new simple stack stub which represents the given stack manipulation.
         *
         * @param stackManipulations The stack manipulations to apply for this stack stub in their application order.
         */
        public Simple(List<? extends StackManipulation> stackManipulations) {
            this.stackManipulation = new StackManipulation.Compound(stackManipulations);
        }

        @Override
        public StackManipulation eval(Target implementationTarget, MethodDescription instrumentedMethod) {
            return stackManipulation;
        }

        @Override
        public boolean equals(Object other) {
            return this == other || !(other == null || getClass() != other.getClass())
                                    && stackManipulation.equals(((StackStub.Simple) other).stackManipulation);
        }

        @Override
        public int hashCode() {
            return stackManipulation.hashCode();
        }

        @Override
        public String toString() {
            return "StackStub.Simple{stackManipulation=" + stackManipulation + '}';
        }
    }

    /**
     * An immutable stack manipulation that aggregates a sequence of other stack manipulations.
     */
    class Compound implements StackStub {
        private final List<? extends StackStub> stackStubs;

        /**
         * Creates a new compound stack stub.
         *
         * @param stackManipulation The stack manipulations to be composed in the order of their composition.
         */
        public Compound(StackStub... stackManipulation) {
            this(Arrays.asList(stackManipulation));
        }

        /**
         * Creates a new compound stack stub.
         *
         * @param stackStubs The stack stubs to be composed in the order of their composition.
         */
        public Compound(List<? extends StackStub> stackStubs) {
            this.stackStubs = stackStubs;
        }

        /**
         * Replaces the {@link #eval(Target, MethodDescription)} method with a given compositor. The compositor function
         * is called with the results of calling {@link #eval(Target, MethodDescription)} on the components of this
         * compound stack stub and should return a new stack stub delegate with the proper {@link #eval(Target,
         * MethodDescription)} method.
         *
         * @param compositor the function composing the resulting {@link StackManipulation}s of the components of this
         *                   stack stub
         * @return a new {@link StackStub} with overridden {@link #eval(Target, MethodDescription)} method
         */
        public StackStub withEvalCompositor(Function<StackManipulation[], StackStub> compositor) {
            return withEval((implementationTarget, instrumentedMethod) -> {
                List<StackManipulation> stackManipulations = evalComponents(implementationTarget, instrumentedMethod);
                StackStub composite = compositor.apply(stackManipulations.toArray(new StackManipulation[0]));
                return composite.eval(implementationTarget, instrumentedMethod);
            });
        }

        @Override
        public StackManipulation eval(Target implementationTarget, MethodDescription instrumentedMethod) {
            return new StackManipulation.Compound(evalComponents(implementationTarget, instrumentedMethod));
        }

        protected List<StackManipulation> evalComponents(Target implementationTarget,
                                                         MethodDescription instrumentedMethod) {
            return stackStubs.stream()
                    .map(stackBuilder -> stackBuilder.eval(implementationTarget, instrumentedMethod))
                    .collect(Collectors.toList());
        }

        @Override
        public InstrumentedType prepare(InstrumentedType instrumentedType) {
            for (Implementation implementation : this.stackStubs) {
                instrumentedType = implementation.prepare(instrumentedType);
            }
            return instrumentedType;
        }

        @Override
        public boolean equals(Object other) {
            return this == other || !(other == null || getClass() != other.getClass())
                                    && stackStubs.equals(((StackStub.Compound) other).stackStubs);
        }

        @Override
        public int hashCode() {
            return stackStubs.hashCode();
        }

        @Override
        public String toString() {
            return "StackStub.Compound{stackStubs=" + stackStubs + "}";
        }
    }

    /**
     * Stack stub delegating its methods to distinct instances used for the {@link #withEval(StackStub)}
     * implementation.
     */
    class WithEval implements StackStub {
        private final StackStub implementationDelegate;
        private final StackStub evalDelegate;

        public WithEval(StackStub implementationDelegate, StackStub evalDelegate) {
            this.implementationDelegate = implementationDelegate;
            this.evalDelegate = evalDelegate;
        }

        @Override
        public InstrumentedType prepare(InstrumentedType instrumentedType) {
            return implementationDelegate.prepare(instrumentedType);
        }

        @Override
        public StackManipulation eval(Target implementationTarget, MethodDescription instrumentedMethod) {
            return evalDelegate.eval(implementationTarget, instrumentedMethod);
        }
    }
}

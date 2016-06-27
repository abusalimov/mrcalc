package com.abusalimov.mrcalc.backend.impl.bytebuddy;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.jar.asm.MethodVisitor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Eldar Abusalimov
 */
public interface StackStub extends Implementation {
    StackManipulation eval(Target implementationTarget, MethodDescription instrumentedMethod);

    @Override
    default InstrumentedType prepare(InstrumentedType instrumentedType) {
        return instrumentedType;
    }

    @Override
    default ByteCodeAppender appender(Target implementationTarget) {
        return new Appender(implementationTarget, this);
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
        private final List<? extends StackStub> stackBuilders;

        /**
         * Creates a new compound stack stub.
         *
         * @param stackManipulation The stack manipulations to be composed in the order of their composition.
         */
        public Compound(StackStub... stackManipulation) {
            this(Arrays.asList(stackManipulation));
        }

        /**
         * Creates a new compound stack manipulation.
         *
         * @param stackBuilders The stack manipulations to be composed in the order of their composition.
         */
        public Compound(List<? extends StackStub> stackBuilders) {
            this.stackBuilders = stackBuilders;
        }

        @Override
        public StackManipulation eval(Target implementationTarget, MethodDescription instrumentedMethod) {
            return new StackManipulation.Compound(
                    stackBuilders.stream()
                            .map(stackBuilder -> stackBuilder.eval(implementationTarget, instrumentedMethod))
                            .collect(Collectors.toList()));
        }

        @Override
        public boolean equals(Object other) {
            return this == other || !(other == null || getClass() != other.getClass())
                                    && stackBuilders.equals(((StackStub.Compound) other).stackBuilders);
        }

        @Override
        public int hashCode() {
            return stackBuilders.hashCode();
        }

        @Override
        public String toString() {
            return "StackStub.Compound{stackBuilders=" + stackBuilders + "}";
        }
    }

    class ForImplementation implements StackStub {
        private final Implementation implementation;

        public ForImplementation(Implementation implementation) {
            this.implementation = implementation;
        }

        @Override
        public StackManipulation eval(Target implementationTarget, MethodDescription instrumentedMethod) {
            throw new UnsupportedOperationException("unused");
        }

        @Override
        public ByteCodeAppender appender(Target implementationTarget) {
            return implementation.appender(implementationTarget);
        }

        @Override
        public InstrumentedType prepare(InstrumentedType instrumentedType) {
            return implementation.prepare(instrumentedType);
        }

        @Override
        public boolean equals(Object other) {
            return this == other || !(other == null || getClass() != other.getClass())
                                    && implementation.equals(((ForImplementation) other).implementation);
        }

        @Override
        public int hashCode() {
            return implementation.hashCode();
        }

        @Override
        public String toString() {
            return "StackStub.ForImplementation{implementation=" + implementation + "}";
        }
    }

    /**
     */
    class Appender implements ByteCodeAppender {
        private final Target implementationTarget;
        private final StackStub stackStub;

        public Appender(Target implementationTarget, StackStub... stackStubs) {
            this(implementationTarget, Arrays.asList(stackStubs));
        }

        /**
         * Creates a new simple stack stub which represents the given stack manipulation.
         *
         * @param implementationTarget The implementation target of the current implementation.
         * @param stackStubs The stack manipulations to apply for this stack stub in their application order.
         */
        public Appender(Target implementationTarget, List<? extends StackStub> stackStubs) {
            this.implementationTarget = implementationTarget;
            this.stackStub = new StackStub.Compound(stackStubs);
        }

        @Override
        public Size apply(MethodVisitor methodVisitor,
                           Implementation.Context implementationContext,
                           MethodDescription instrumentedMethod) {
            StackManipulation stackManipulation = stackStub.eval(implementationTarget, instrumentedMethod);
            StackManipulation.Size stackSize = stackManipulation.apply(methodVisitor, implementationContext);
            return new Size(stackSize.getMaximalSize(), instrumentedMethod.getStackSize());
        }
    }
}

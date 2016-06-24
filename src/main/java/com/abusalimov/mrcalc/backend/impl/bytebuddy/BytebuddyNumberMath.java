package com.abusalimov.mrcalc.backend.impl.bytebuddy;

import com.abusalimov.mrcalc.backend.NumberMath;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.StackSize;
import net.bytebuddy.implementation.bytecode.constant.LongConstant;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;

import java.util.function.Function;

/**
 * Implements basic number operation for abstract operands. Subclasses should only provide a {@link StackStub}s
 * corresponding to the certain operation by its name.
 *
 * @author Eldar Abusalimov
 */
public enum BytebuddyNumberMath implements NumberMath<Number, StackStub> {
    LONG(LongConstant::forValue, NumberOpStackStub.ForLong::valueOf);

    private final Function<Number, StackManipulation> constantProvider;
    private final Function<String, StackStub> opStackStubProvider;

    <T extends Number> BytebuddyNumberMath(Function<T, StackManipulation> constantProvider,
                                           Function<String, StackStub> opStackStubProvider) {
        //noinspection unchecked
        this.constantProvider = (Function<Number, StackManipulation>) constantProvider;
        this.opStackStubProvider = opStackStubProvider;
    }

    public static <T extends Number> BytebuddyNumberMath forType(Class<T> type) {
        if (type == long.class) {
            return LONG;
        } else {
            throw new UnsupportedOperationException("Unknown Number class " + type);
        }
    }

    @Override
    public StackStub constant(Number literal) {
        return new StackStub.Simple(constantProvider.apply(literal));
    }

    @Override
    public StackStub add(StackStub leftOperand, StackStub rightOperand) {
        return new StackStub.Compound(leftOperand, rightOperand, getOpStackStub("ADD"));
    }

    @Override
    public StackStub sub(StackStub leftOperand, StackStub rightOperand) {
        return new StackStub.Compound(leftOperand, rightOperand, getOpStackStub("SUB"));
    }

    @Override
    public StackStub mul(StackStub leftOperand, StackStub rightOperand) {
        return new StackStub.Compound(leftOperand, rightOperand, getOpStackStub("MUL"));
    }

    @Override
    public StackStub div(StackStub leftOperand, StackStub rightOperand) {
        return new StackStub.Compound(leftOperand, rightOperand, getOpStackStub("DIV"));
    }

    @Override
    public StackStub pow(StackStub leftOperand, StackStub rightOperand) {
        throw new RuntimeException("NIY pow");
    }

    @Override
    public StackStub neg(StackStub operand) {
        return new StackStub.Compound(operand, getOpStackStub("NEG"));
    }

    private StackStub getOpStackStub(String name) {
        return opStackStubProvider.apply(name);
    }

    /**
     * Default implementations of {@link StackStub} methods for number operations.
     *
     * @author Eldar Abusalimov
     */
    protected interface NumberOpStackStub extends StackManipulation, StackStub {

        @Override
        default Size apply(MethodVisitor methodVisitor, Implementation.Context implementationContext) {
            methodVisitor.visitInsn(getOpcode());
            int sizeImpact = getOperandSizeImpact();
            int growth = Math.max(0, sizeImpact);
            return new Size(sizeImpact, growth);
        }

        @Override
        default boolean isValid() {
            return true;
        }

        @Override
        default StackManipulation eval(MethodDescription instrumentedMethod) {
            return this;
        }

        /**
         * Returns the instruction opcode to emit.
         *
         * @return the {@link Opcodes opcode}
         */
        int getOpcode();

        /**
         * Returns the size impact of this number operation. Note that the size change depends on the {@link StackSize}
         * of the operands involved.
         *
         * @return the size change of the stack after executing the instruction
         */
        int getOperandSizeImpact();

        /**
         * Stack stubs of instructions for math operation on longs.
         */
        enum ForLong implements NumberOpStackStub {
            ADD(Opcodes.LADD, -1),
            SUB(Opcodes.LSUB, -1),
            MUL(Opcodes.LMUL, -1),
            DIV(Opcodes.LDIV, -1),
            NEG(Opcodes.LNEG, 0);

            private final int opcode;
            private final int operandStackImpact;

            ForLong(int opcode, int operandStackImpact) {
                this.opcode = opcode;
                this.operandStackImpact = operandStackImpact;
            }

            @Override
            public int getOperandSizeImpact() {
                return operandStackImpact * StackSize.of(long.class).getSize();
            }

            @Override
            public int getOpcode() {
                return opcode;
            }
        }
    }
}
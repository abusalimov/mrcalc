package com.abusalimov.mrcalc.backend.impl.bytebuddy;

import com.abusalimov.mrcalc.backend.NumberMath;
import net.bytebuddy.implementation.bytecode.StackSize;
import net.bytebuddy.implementation.bytecode.constant.LongConstant;
import net.bytebuddy.jar.asm.Opcodes;

/**
 * Implements numeric math on primitive longs.
 *
 * @author Eldar Abusalimov
 */
public enum BytebuddyLongMath implements NumberMath<Long, StackStub> {
    /**
     * The singleton instance.
     */
    INSTANCE;

    @Override
    public StackStub constant(Long literal) {
        return instrumentedMethod -> LongConstant.forValue(literal);
    }

    @Override
    public StackStub add(StackStub leftOperand, StackStub rightOperand) {
        return new StackStub.Compound(leftOperand, rightOperand, OpStackStub.ADD);
    }

    @Override
    public StackStub sub(StackStub leftOperand, StackStub rightOperand) {
        return new StackStub.Compound(leftOperand, rightOperand, OpStackStub.SUB);
    }

    @Override
    public StackStub mul(StackStub leftOperand, StackStub rightOperand) {
        return new StackStub.Compound(leftOperand, rightOperand, OpStackStub.MUL);
    }

    @Override
    public StackStub div(StackStub leftOperand, StackStub rightOperand) {
        return new StackStub.Compound(leftOperand, rightOperand, OpStackStub.DIV);
    }

    @Override
    public StackStub pow(StackStub leftOperand, StackStub rightOperand) {
        throw new RuntimeException("NIY pow");
    }

    @Override
    public StackStub neg(StackStub operand) {
        return new StackStub.Compound(operand, OpStackStub.NEG);
    }

    /**
     * Stack stubs of instructions for math operation on longs.
     */
    protected enum OpStackStub implements NumberOpStackStub {
        ADD(Opcodes.LADD, -1),
        SUB(Opcodes.LSUB, -1),
        MUL(Opcodes.LMUL, -1),
        DIV(Opcodes.LDIV, -1),
        NEG(Opcodes.LNEG, 0);

        private final int opcode;
        private final int operandStackImpact;

        OpStackStub(int opcode, int operandStackImpact) {
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

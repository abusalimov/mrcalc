package com.abusalimov.mrcalc.backend.impl.bytebuddy;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.jar.asm.MethodVisitor;

/**
 * Default implementations of {@link StackStub} methods for number operations.
 *
 * @author Eldar Abusalimov
 */
public interface NumberOpStackStub extends StackManipulation, StackStub {
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
     * @return the {@link net.bytebuddy.jar.asm.Opcodes opcode}
     */
    int getOpcode();

    /**
     * Returns the size impact of this number operation. Note that the size change depends on the {@link
     * net.bytebuddy.implementation.bytecode.StackSize} of the operands involved.
     *
     * @return the size change of the stack after executing the instruction
     */
    int getOperandSizeImpact();
}

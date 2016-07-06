package com.abusalimov.mrcalc.backend.impl.bytebuddy;

import com.abusalimov.mrcalc.backend.SequenceReduce;
import com.abusalimov.mrcalc.runtime.Sequence;

import java.util.function.BinaryOperator;
import java.util.function.DoubleBinaryOperator;
import java.util.function.LongBinaryOperator;

/**
 * @author Eldar Abusalimov
 */
public enum BytebuddySequenceReduce implements SequenceReduce<StackStub, StackStub, StackStub> {
    OBJECT("reduce", Sequence.class, Object.class, BinaryOperator.class),
    LONG("reduceLong", Sequence.OfLong.class, long.class, LongBinaryOperator.class),
    DOUBLE("reduceDouble", Sequence.OfDouble.class, double.class, DoubleBinaryOperator.class);

    private final RuntimeMethodInvoke runtimeMethodInvoke;

    BytebuddySequenceReduce(String runtimeMethodName, Class<?>... parameterTypes) {
        runtimeMethodInvoke = new RuntimeMethodInvoke(runtimeMethodName, parameterTypes);
    }

    public static BytebuddySequenceReduce forType(Class<?> type) {
        if (type.isPrimitive()) {
            return BytebuddySequenceReduce.valueOf(type.getName().toUpperCase());
        } else {
            return OBJECT;
        }
    }

    @Override
    public StackStub reduce(StackStub sequence, StackStub neutral, StackStub lambda) {
        return new StackStub.Compound(sequence, neutral, lambda)
                .withEvalCompositor(runtimeMethodInvoke::invokeWithArguments);
    }
}

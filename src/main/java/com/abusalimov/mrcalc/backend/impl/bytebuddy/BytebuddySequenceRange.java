package com.abusalimov.mrcalc.backend.impl.bytebuddy;

import com.abusalimov.mrcalc.backend.SequenceRange;
import com.abusalimov.mrcalc.runtime.Runtime;

import java.lang.reflect.Method;

/**
 * @author Eldar Abusalimov
 */
public enum BytebuddySequenceRange implements SequenceRange<StackStub, StackStub> {
    LONG("createLongRangeInclusive", long.class);

    private final Method runtimeMethod;

    BytebuddySequenceRange(String runtimeMethodName, Class<? extends Number> boundaryType) {
        try {
            this.runtimeMethod = Runtime.class.getDeclaredMethod(runtimeMethodName, boundaryType, boundaryType);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(String.format("Couldn't find Runtime method '%s'", runtimeMethodName), e);
        }
    }

    public static <T extends Number> BytebuddySequenceRange forType(Class<T> type) {
        if (type.isPrimitive()) {
            return BytebuddySequenceRange.valueOf(type.getName().toUpperCase());
        } else {
            throw new UnsupportedOperationException("Unknown Number class " + type);
        }
    }

    @Override
    public StackStub range(StackStub start, StackStub end) {
        return new StackStub.Compound(start, end)
                .withEvalCompositor(
                        stackManipulations -> RawMethodCall.invokeRuntime(runtimeMethod, stackManipulations));
    }
}

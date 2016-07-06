package com.abusalimov.mrcalc.backend.impl.bytebuddy;

import com.abusalimov.mrcalc.backend.SequenceRange;

/**
 * @author Eldar Abusalimov
 */
public enum BytebuddySequenceRange implements SequenceRange<StackStub, StackStub> {
    LONG("createLongRangeInclusive", long.class);

    private final RuntimeMethodInvoke runtimeMethodInvoke;

    BytebuddySequenceRange(String runtimeMethodName, Class<? extends Number> boundaryType) {
        runtimeMethodInvoke = new RuntimeMethodInvoke(runtimeMethodName, boundaryType, boundaryType);
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
                .withEvalCompositor(runtimeMethodInvoke::invokeWithArguments);
    }
}

package com.abusalimov.mrcalc.backend.impl.bytebuddy;

import com.abusalimov.mrcalc.backend.SequenceMap;
import com.abusalimov.mrcalc.runtime.Sequence;

import java.util.function.*;

/**
 * @author Eldar Abusalimov
 */
public enum BytebuddySequenceMap {
    OBJECT(FromAny.ToObject::valueOf),
    LONG(FromAny.ToLong::valueOf),
    DOUBLE(FromAny.ToDouble::valueOf);

    private final Function<String, SequenceMap<StackStub, StackStub, StackStub>> delegateValueOf;

    BytebuddySequenceMap(Function<String, SequenceMap<StackStub, StackStub, StackStub>> delegateValueOf) {
        this.delegateValueOf = delegateValueOf;
    }

    public static String nameForType(Class<?> type) {
        return type.isPrimitive() ? type.getName().toUpperCase() : "OBJECT";
    }

    public static SequenceMap<StackStub, StackStub, StackStub> forType(Class<?> returnElementType,
                                                                       Class<?> elementType) {
        return BytebuddySequenceMap.valueOf(nameForType(returnElementType)).forElementType(elementType);
    }

    public SequenceMap<StackStub, StackStub, StackStub> forElementType(Class<?> elementType) {
        return delegateValueOf.apply(nameForType(elementType));
    }

    protected interface FromAny extends SequenceMap<StackStub, StackStub, StackStub> {
        @Override
        default StackStub map(StackStub sequence, StackStub lambda) {
            return new StackStub.Compound(sequence, lambda)
                    .withEvalCompositor(getRuntimeMethodInvoke()::invokeWithArguments);
        }

        RuntimeMethodInvoke getRuntimeMethodInvoke();

        enum ToObject implements FromAny {
            OBJECT("mapToObject", Sequence.class, Function.class),
            LONG("mapLongToObject", Sequence.OfLong.class, LongFunction.class),
            DOUBLE("mapDoubleToObject", Sequence.OfDouble.class, DoubleFunction.class);

            private final RuntimeMethodInvoke runtimeMethodInvoke;

            ToObject(String runtimeMethodName, Class<?>... parameterTypes) {
                runtimeMethodInvoke = new RuntimeMethodInvoke(runtimeMethodName, parameterTypes);
            }

            @Override
            public RuntimeMethodInvoke getRuntimeMethodInvoke() {
                return runtimeMethodInvoke;
            }
        }

        enum ToLong implements FromAny {
            OBJECT("mapToLong", Sequence.class, ToLongFunction.class),
            LONG("mapLongToLong", Sequence.OfLong.class, LongUnaryOperator.class),
            DOUBLE("mapDoubleToLong", Sequence.OfDouble.class, DoubleToLongFunction.class);

            private final RuntimeMethodInvoke runtimeMethodInvoke;

            ToLong(String runtimeMethodName, Class<?>... parameterTypes) {
                runtimeMethodInvoke = new RuntimeMethodInvoke(runtimeMethodName, parameterTypes);
            }

            @Override
            public RuntimeMethodInvoke getRuntimeMethodInvoke() {
                return runtimeMethodInvoke;
            }
        }

        enum ToDouble implements FromAny {
            OBJECT("mapToDouble", Sequence.class, ToDoubleFunction.class),
            LONG("mapLongToDouble", Sequence.OfLong.class, LongToDoubleFunction.class),
            DOUBLE("mapDoubleToDouble", Sequence.OfDouble.class, DoubleUnaryOperator.class);

            private final RuntimeMethodInvoke runtimeMethodInvoke;

            ToDouble(String runtimeMethodName, Class<?>... parameterTypes) {
                runtimeMethodInvoke = new RuntimeMethodInvoke(runtimeMethodName, parameterTypes);
            }

            @Override
            public RuntimeMethodInvoke getRuntimeMethodInvoke() {
                return runtimeMethodInvoke;
            }
        }
    }
}

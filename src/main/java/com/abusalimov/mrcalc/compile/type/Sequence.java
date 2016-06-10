package com.abusalimov.mrcalc.compile.type;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author Eldar Abusalimov
 */
public class Sequence implements Type {
    private final Primitive primitive;
    private final int sequenceDepth;

    public Sequence(Primitive primitive) {
        this(primitive, 1);
    }

    public Sequence(Primitive primitive, int sequenceDepth) {
        if (sequenceDepth < 1) {
            throw new IllegalArgumentException("Sequence depth must be positive");
        }
        this.primitive = Objects.requireNonNull(primitive);
        this.sequenceDepth = sequenceDepth;
    }

    public static Sequence of(Type type) {
        int sequenceDepth = (type instanceof Sequence) ? ((Sequence) type).getSequenceDepth() : 0;
        return new Sequence(type.getPrimitive(), sequenceDepth + 1);
    }

    public Type getElementType() {
        if (sequenceDepth == 1) {
            return getPrimitive();
        } else {
            return new Sequence(primitive, sequenceDepth - 1);
        }
    }

    @Override
    public Primitive getPrimitive() {
        return primitive;
    }

    @Override
    public Class<?> getTypeClass() {
        return Object.class;
    }

    public int getSequenceDepth() {
        return sequenceDepth;
    }

    @Override
    public int hashCode() {
        return Objects.hash(primitive, sequenceDepth);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Sequence)) {
            return false;
        }
        Sequence another = (Sequence) obj;
        return (this.primitive == another.primitive &&
                this.sequenceDepth == another.sequenceDepth);
    }

    @Override
    public String toString() {
        return primitive.toString() + StringUtils.repeat("[]", sequenceDepth);
    }
}

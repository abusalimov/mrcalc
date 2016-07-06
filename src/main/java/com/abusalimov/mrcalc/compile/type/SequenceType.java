package com.abusalimov.mrcalc.compile.type;

import com.abusalimov.mrcalc.runtime.Sequence;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * Represents a sequence type of certain depth (dimension).
 *
 * @author Eldar Abusalimov
 */
public class SequenceType implements Type {
    private final PrimitiveType primitiveType;
    private final int sequenceDepth;

    /**
     * Creates a new instance with the specified underlying primitiveType at the given depth.
     *
     * @param primitiveType the underlying primitiveType
     * @param sequenceDepth the sequence dimension
     */
    private SequenceType(PrimitiveType primitiveType, int sequenceDepth) {
        if (sequenceDepth < 1) {
            throw new IllegalArgumentException("Sequence depth must be positive");
        }
        this.primitiveType = Objects.requireNonNull(primitiveType);
        this.sequenceDepth = sequenceDepth;
    }

    /**
     * Creates a new instance representing a sequence of elements of the given type.
     *
     * @param type the element type
     * @return the new {@link SequenceType} instance
     */
    public static SequenceType of(Type type) {
        int sequenceDepth = (type instanceof SequenceType) ? ((SequenceType) type).getSequenceDepth() : 0;
        return new SequenceType(type.getPrimitiveType(), sequenceDepth + 1);
    }

    public Type getElementType() {
        if (sequenceDepth == 1) {
            return getPrimitiveType();
        } else {
            return new SequenceType(primitiveType, sequenceDepth - 1);
        }
    }

    public PrimitiveType getPrimitiveType() {
        return primitiveType;
    }

    @Override
    public Class<?> getTypeClass() {
        if (sequenceDepth != 1) {
            return Sequence.class;
        }
        switch (primitiveType) {
            case INTEGER:
                return Sequence.OfLong.class;
            case FLOAT:
                return Sequence.OfDouble.class;
            case UNKNOWN:
            default:
                return null;
        }
    }

    public int getSequenceDepth() {
        return sequenceDepth;
    }

    @Override
    public int hashCode() {
        return Objects.hash(primitiveType, sequenceDepth);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SequenceType)) {
            return false;
        }
        SequenceType another = (SequenceType) obj;
        return (this.primitiveType == another.primitiveType &&
                this.sequenceDepth == another.sequenceDepth);
    }

    @Override
    public String toString() {
        return primitiveType.toString() + StringUtils.repeat("[]", sequenceDepth);
    }
}

package com.abusalimov.mrcalc.compile.type;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The primitive numeric scalar types enumeration.
 *
 * @author Eldar Abusalimov
 */
public enum PrimitiveType implements Type {
    INTEGER("integer", Long.class),
    FLOAT("float", Double.class),

    /**
     * The UNKNOWN type is a special case. It is used as a placeholder when the proper type of the expression can't be
     * inferred because of type errors.
     */
    UNKNOWN("?", null);

    private final String name;
    private final Class<? extends Number> typeClass;

    PrimitiveType(String name, Class<? extends Number> typeClass) {
        this.name = name;
        this.typeClass = typeClass;
    }

    /**
     * The same as {@link #promote(List)}, but accepts vararg.
     *
     * @param types the types to promote
     * @return the most wide type
     * @see #promote(List)
     */
    public static PrimitiveType promote(PrimitiveType... types) {
        return promote(Arrays.asList(types));
    }

    /**
     * Promotes the given types to a common one using widening rules:
     * <p>
     * integer -> float -> UNKNOWN
     *
     * @param types the list of types to promote
     * @return the the most wide type of the specified types, or {@link #UNKNOWN} in case the list is empty
     */
    public static PrimitiveType promote(List<PrimitiveType> types) {
        if (types.size() > 0) {
            return Collections.max(types);
        }
        return UNKNOWN;
    }

    public PrimitiveType getPrimitiveType() {
        return this;
    }

    @Override
    public Class<? extends Number> getTypeClass() {
        return typeClass;
    }

    @Override
    public String toString() {
        return name;
    }
}

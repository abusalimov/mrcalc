package com.abusalimov.mrcalc.compile.type;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Eldar Abusalimov
 */
public enum Primitive implements Type {
    INTEGER("integer", Long.class),
    FLOAT("float", Double.class),
    UNKNOWN("?", null);

    private final String name;
    private final Class<? extends Number> typeClass;

    Primitive(String name, Class<? extends Number> typeClass) {
        this.name = name;
        this.typeClass = typeClass;
    }

    public static Primitive promote(Primitive... types) {
        return promote(Arrays.asList(types));
    }

    public static Primitive promote(List<Primitive> types) {
        if (types.size() > 0) {
            return Collections.max(types);
        }
        return UNKNOWN;
    }

    @Override
    public Primitive getPrimitive() {
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

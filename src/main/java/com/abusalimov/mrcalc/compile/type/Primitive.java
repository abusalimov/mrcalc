package com.abusalimov.mrcalc.compile.type;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Eldar Abusalimov
 */
public enum Primitive implements Type {
    INTEGER("integer"),
    FLOAT("float"),
    UNKNOWN("?");

    private final String name;

    Primitive(String name) {
        this.name = name;
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
    public String toString() {
        return name;
    }
}

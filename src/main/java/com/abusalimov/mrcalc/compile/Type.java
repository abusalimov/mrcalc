package com.abusalimov.mrcalc.compile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Eldar Abusalimov
 */
public enum Type {
    INTEGER("integer"),
    FLOAT("float"),
    UNKNOWN("?");

    private final String name;

    Type(String name) {
        this.name = name;
    }

    public static Type promote(Type... types) {
        return promote(Arrays.asList(types));
    }

    public static Type promote(List<Type> types) {
        if (types.size() > 0) {
            return Collections.max(types);
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return name;
    }
}

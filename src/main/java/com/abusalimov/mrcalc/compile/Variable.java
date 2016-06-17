package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.compile.type.Type;

import java.util.Objects;

/**
 * Immutable representation of a variable declaration.
 *
 * @author Eldar Abusalimov
 */
public class Variable {
    private final String name;
    private final Type type;

    /**
     * Creates a new variable with give name and type.
     *
     * @param name a non-{@code null} string
     * @param type a non-{@code null} {@link Type} instance
     */
    public Variable(String name, Type type) {
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
    }

    /**
     * Returns the name identifying the variable.
     *
     * @return a non-{@code null} string
     */
    public String getName() {
        return name;
    }

    /**
     * Return the type inferred for this variable.
     *
     * @return a non-{@code null} type (this may be
     * {@link com.abusalimov.mrcalc.compile.type.Primitive#UNKNOWN} though)
     */
    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return name + " : " + type;
    }
}

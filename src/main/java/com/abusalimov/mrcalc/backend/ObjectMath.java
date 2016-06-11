package com.abusalimov.mrcalc.backend;

import java.util.function.Function;

/**
 * @author Eldar Abusalimov
 */
public interface ObjectMath<T, E extends Expr, F extends Expr> {
    Function<Object[], ?> toFunction(E expr);

    E load(String name, int slot);
    E constant(T literal);

    F map(F sequence, E lambda);
    E reduce(F sequence, E neutral, E lambda);
}

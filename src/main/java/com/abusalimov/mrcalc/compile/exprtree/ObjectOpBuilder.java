package com.abusalimov.mrcalc.compile.exprtree;

import java.util.function.Function;

/**
 * @author Eldar Abusalimov
 */
public interface ObjectOpBuilder<T, E extends Expr<T>, I extends Expr<Long>> {
    Function<Object[], T> toFunction(E expr);

    E load(String name, int slot);

    E range(I startOperand, I endOperand);
}

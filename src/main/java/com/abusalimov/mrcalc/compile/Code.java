package com.abusalimov.mrcalc.compile;

import java.util.function.Function;

/**
 * TODO stub implementation
 *
 * @author Eldar Abusalimov
 */
public class Code {
    private final Function<Object[], ?> exprFunction;

    public Code(Function<Object[], ?> exprFunction) {
        this.exprFunction = exprFunction;
    }

    public Function<Object[], ?> getExprFunction() {
        return exprFunction;
    }
}

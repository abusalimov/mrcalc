package com.abusalimov.mrcalc.backend;

/**
 * @author Eldar Abusalimov
 */
public interface NumberCast<F extends Expr, E extends Expr> {
    E cast(F expr);
}

package com.abusalimov.mrcalc.backend;

/**
 * @author Eldar Abusalimov
 */
public interface NumberMath<T extends Number, E extends Expr, F extends Expr> extends ObjectMath<T, E, F> {
    E add(E leftOperand, E rightOperand);
    E sub(E leftOperand, E rightOperand);
    E mul(E leftOperand, E rightOperand);
    E div(E leftOperand, E rightOperand);
    E pow(E leftOperand, E rightOperand);
    E neg(E operand);

    F range(E start, E end);
}

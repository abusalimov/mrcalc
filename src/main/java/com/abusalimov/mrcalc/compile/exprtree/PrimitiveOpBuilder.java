package com.abusalimov.mrcalc.compile.exprtree;

/**
 * @author Eldar Abusalimov
 */
public interface PrimitiveOpBuilder<T extends Number, E extends Expr<T, E>> {
    E load(String name, int slot);
    E constant(T literal);
    E add(E leftOperand, E rightOperand);
    E sub(E leftOperand, E rightOperand);
    E mul(E leftOperand, E rightOperand);
    E div(E leftOperand, E rightOperand);
    E pow(E leftOperand, E rightOperand);
    E neg(E operand);
}

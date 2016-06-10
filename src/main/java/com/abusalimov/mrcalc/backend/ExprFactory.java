package com.abusalimov.mrcalc.backend;

/**
 * @author Eldar Abusalimov
 */
public interface ExprFactory<E extends Expr> {
    <T> ObjectMath<T, E, E> getObjectMath(Class<T> returnType);

    <T extends Number> NumberMath<T, E, E> getNumberMath(Class<T> returnType);

    NumberCast<E, E> getNumberCast(Class<? extends Number> toType, Class<? extends Number> fromType);
}

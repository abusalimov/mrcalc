package com.abusalimov.mrcalc.backend;

/**
 * The backend is responsible for assembling expressions into callable functions.
 *
 * @param <E> the type of expressions used by the backend implementation
 * @author Eldar Abusalimov
 */
public interface Backend<E> {
    /**
     * Gets an {@link ObjectMath} instance suitable for assembling expressions of the given type.
     *
     * @param returnType the required class of expressions being assembled
     * @param <T>        the class type argument
     * @return the {@link ObjectMath} instance
     */
    <T> ObjectMath<T, E, E> getObjectMath(Class<T> returnType);

    /**
     * Gets a {@link NumberMath} instance suitable for assembling expressions of the given numeric type.
     *
     * @param returnType the required class of numeric expressions being assembled
     * @param <T>        the class type argument
     * @return the {@link NumberMath} instance
     */
    <T extends Number> NumberMath<T, E, E> getNumberMath(Class<T> returnType);

    /**
     * Gets a {@link NumberCast} instance providing a method for numeric conversion between the specified types.
     *
     * @param toType   the result type of the required conversion
     * @param fromType the original type of the conversion
     * @return the {@link NumberCast} instance for numeric conversions
     */
    NumberCast<E, E> getNumberCast(Class<? extends Number> toType, Class<? extends Number> fromType);
}

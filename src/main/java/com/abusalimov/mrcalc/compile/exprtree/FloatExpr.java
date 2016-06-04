package com.abusalimov.mrcalc.compile.exprtree;

import java.util.function.DoubleSupplier;

/**
 * @author Eldar Abusalimov
 */
public interface FloatExpr extends Expr, DoubleSupplier {
    @Override
    default Type getType() {
        return Type.FLOAT;
    }

    @Override
    default Expr cast(Type type) {
        switch (type) {
            case INTEGER:
                return (IntegerExpr) () -> (long) getAsDouble();
            case FLOAT:
                return this;
            case UNKNOWN:
            default:
                return INVALID;
        }
    }
}

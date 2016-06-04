package com.abusalimov.mrcalc.compile.exprtree;

import java.util.function.LongSupplier;

/**
 * @author Eldar Abusalimov
 */
public interface IntegerExpr extends Expr, LongSupplier {
    @Override
    default Type getType() {
        return Type.INTEGER;
    }

    @Override
    default Expr cast(Type type) {
        switch (type) {
            case INTEGER:
                return this;
            case FLOAT:
                return (FloatExpr) () -> (double) getAsLong();
            case UNKNOWN:
            default:
                return INVALID;
        }
    }
}

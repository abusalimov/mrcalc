package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.ObjectOpBuilder;

import java.util.function.Function;

/**
 * @author Eldar Abusalimov
 */
public class ObjectFuncOpBuilder
        implements ObjectOpBuilder<Object, ObjectFuncExpr, IntegerFuncExpr> {

    @Override
    public Function<Object[], Object> toFunction(ObjectFuncExpr expr) {
        return expr;
    }

    @Override
    public ObjectFuncExpr load(String name, int slot) {
        return args -> args[slot];
    }

    @Override
    public ObjectFuncExpr range(IntegerFuncExpr startOperand, IntegerFuncExpr endOperand) {
        return args -> {
            int start = (int) startOperand.applyAsLong(args);
            int end = (int) endOperand.applyAsLong(args);
            int length = Math.max(0, end - start + 1);
            long[] ret = new long[length];
            for (int i = 0; i < length; i++) {
                ret[i] = start + i;
            }
            return ret;
        };
    }
}

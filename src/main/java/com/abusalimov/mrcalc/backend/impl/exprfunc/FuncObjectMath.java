package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.ObjectMath;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * @author Eldar Abusalimov
 */
public class FuncObjectMath<T> implements ObjectMath<T, FuncExpr<T>, FuncExpr<List<?>>> {
    public static final FuncObjectMath INSTANCE = new FuncObjectMath();

    @Override
    public Function<Object[], ?> toFunction(FuncExpr<T> expr) {
        return expr;
    }

    @Override
    public FuncExpr<T> load(String name, int slot) {
        return args -> (T) args[slot];
    }

    @Override
    public FuncExpr<T> constant(T literal) {
        return args -> literal;
    }

    @Override
    public FuncExpr<List<?>> map(FuncExpr<List<?>> sequenceExpr, FuncExpr<T> lambda) {
        return args -> {
            List<?> sequence = sequenceExpr.apply(args);
            int length = sequence.size();
            T[] ret = (T[]) new Object[length];
            for (int i = 0; i < length; i++) {
                ret[i] = lambda.apply(new Object[]{sequence.get(i)});
            }
            return Arrays.asList(ret);
        };
    }

    @Override
    public FuncExpr<T> reduce(FuncExpr<List<?>> sequenceExpr, FuncExpr<T> neutral, FuncExpr<T> lambda) {
        return args -> {
            List<?> sequence = sequenceExpr.apply(args);
            T ret = neutral.apply(args);
            for (Object element : sequence) {
                ret = lambda.apply(new Object[]{ret, element});
            }
            return ret;
        };
    }
}

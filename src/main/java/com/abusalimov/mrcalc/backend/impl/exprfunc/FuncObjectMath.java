package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.ObjectMath;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implements operations common for all expression types, both numeric and generic.
 *
 * @author Eldar Abusalimov
 */
public class FuncObjectMath<T> implements ObjectMath<T, FuncExpr<T>, FuncExpr<List<?>>> {
    public static final FuncObjectMath INSTANCE = new FuncObjectMath();

    @Override
    public Function<Object[], ?> toFunction(FuncExpr<T> expr) {
        return expr;
    }

    @SuppressWarnings("unchecked")
    @Override
    public FuncExpr<T> load(int slot, String name) {
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
            return sequence.parallelStream()
                    .map(x -> lambda.apply(new Object[]{x}))
                    .collect(Collectors.toList());
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public FuncExpr<T> reduce(FuncExpr<List<?>> sequenceExpr, FuncExpr<T> neutral, FuncExpr<T> lambda) {
        return args -> {
            List<T> sequence = (List<T>) sequenceExpr.apply(args);
            return sequence.parallelStream()
                    .reduce(neutral.apply(args),
                            (x, y) -> lambda.apply(new Object[]{x, y}));
        };
    }
}

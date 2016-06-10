package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.NumberMath;

import java.util.Arrays;
import java.util.List;

/**
 * @author Eldar Abusalimov
 */
public class LongFuncNumberMath extends FuncObjectMath<Long> implements NumberMath<Long, FuncExpr<Long>, FuncExpr<List<?>>> {
    public static final LongFuncNumberMath INSTANCE = new LongFuncNumberMath();

    @Override
    public FuncExpr<List<?>> range(FuncExpr<Long> startOperand, FuncExpr<Long> endOperand) {
        return args -> {
            int start = startOperand.apply(args).intValue();
            int end = endOperand.apply(args).intValue();
            int length = Math.max(0, end - start + 1);
            Long[] ret = new Long[length];
            for (int i = 0; i < length; i++) {
                ret[i] = (long) start + i;
            }
            return Arrays.asList(ret);
        };
    }

    @Override
    public FuncExpr<Long> add(FuncExpr<Long> leftOperand, FuncExpr<Long> rightOperand) {
        return args -> leftOperand.apply(args) + rightOperand.apply(args);
    }

    @Override
    public FuncExpr<Long> sub(FuncExpr<Long> leftOperand, FuncExpr<Long> rightOperand) {
        return args -> leftOperand.apply(args) - rightOperand.apply(args);
    }

    @Override
    public FuncExpr<Long> mul(FuncExpr<Long> leftOperand, FuncExpr<Long> rightOperand) {
        return args -> leftOperand.apply(args) * rightOperand.apply(args);
    }

    @Override
    public FuncExpr<Long> div(FuncExpr<Long> leftOperand, FuncExpr<Long> rightOperand) {
        return args -> leftOperand.apply(args) / rightOperand.apply(args);
    }

    @Override
    public FuncExpr<Long> pow(FuncExpr<Long> leftOperand, FuncExpr<Long> rightOperand) {
        return args -> (long) Math.pow(leftOperand.apply(args), rightOperand.apply(args));
    }

    @Override
    public FuncExpr<Long> neg(FuncExpr<Long> operand) {
        return args -> -operand.apply(args);
    }
}

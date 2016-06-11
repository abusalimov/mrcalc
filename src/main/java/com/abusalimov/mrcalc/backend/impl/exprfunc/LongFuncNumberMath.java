package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.NumberMath;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * @author Eldar Abusalimov
 */
public class LongFuncNumberMath implements NumberMath<Long, LongFuncExpr, FuncObjectExpr<List<?>>> {
    public static final LongFuncNumberMath INSTANCE = new LongFuncNumberMath();

    @Override
    public Function<Object[], ?> toFunction(LongFuncExpr expr) {
        return expr::applyAsLong;
    }

    @Override
    public LongFuncExpr load(String name, int slot) {
        return args -> (long) args[slot];
    }

    @Override
    public LongFuncExpr constant(Long literal) {
        long primitive = literal;
        return args -> primitive;
    }

    @Override
    public FuncObjectExpr<List<?>> range(LongFuncExpr startOperand, LongFuncExpr endOperand) {
        //noinspection unchecked
        return (FuncObjectExpr) (FuncObjectExpr<long[]>) args -> {
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

    @Override
    public FuncObjectExpr<List<?>> map(FuncObjectExpr<List<?>> sequenceExpr, LongFuncExpr lambda) {
        return args -> {
            List<?> sequence = sequenceExpr.apply(args);
            int length = sequence.size();
            Long[] ret = (Long[]) new Object[length];
            for (int i = 0; i < length; i++) {
                ret[i] = lambda.applyAsLong(new Object[]{sequence.get(i)});
            }
            return Arrays.asList(ret);
        };
    }

    @Override
    public LongFuncExpr reduce(FuncObjectExpr<List<?>> sequenceExpr, LongFuncExpr neutral, LongFuncExpr lambda) {
        FuncObjectExpr<long[]> aryExpr = (FuncObjectExpr) sequenceExpr;
        return args -> {
            long[] sequence = aryExpr.apply(args);
            long ret = neutral.applyAsLong(args);
            Object[] argStub = new Object[0];
            for (Object element : sequence) {
                ret = lambda.applyAsLong(new Object[]{ret, element});
//                ret = lambda.applyAsLong(argStub);
            }
            return ret;
        };
    }

    @Override
    public LongFuncExpr add(LongFuncExpr leftOperand, LongFuncExpr rightOperand) {
        return args -> leftOperand.applyAsLong(args) + rightOperand.applyAsLong(args);
    }

    @Override
    public LongFuncExpr sub(LongFuncExpr leftOperand, LongFuncExpr rightOperand) {
        return args -> leftOperand.applyAsLong(args) - rightOperand.applyAsLong(args);
    }

    @Override
    public LongFuncExpr mul(LongFuncExpr leftOperand, LongFuncExpr rightOperand) {
        return args -> leftOperand.applyAsLong(args) * rightOperand.applyAsLong(args);
    }

    @Override
    public LongFuncExpr div(LongFuncExpr leftOperand, LongFuncExpr rightOperand) {
        return args -> leftOperand.applyAsLong(args) / rightOperand.applyAsLong(args);
    }

    @Override
    public LongFuncExpr pow(LongFuncExpr leftOperand, LongFuncExpr rightOperand) {
        return args -> (long) Math.pow(leftOperand.applyAsLong(args), rightOperand.applyAsLong(args));
    }

    @Override
    public LongFuncExpr neg(LongFuncExpr operand) {
        return args -> -operand.applyAsLong(args);
    }
}

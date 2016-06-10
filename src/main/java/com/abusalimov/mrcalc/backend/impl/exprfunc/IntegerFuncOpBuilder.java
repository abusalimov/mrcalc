package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.PrimitiveOpBuilder;

import java.util.function.Function;

/**
 * @author Eldar Abusalimov
 */
public class IntegerFuncOpBuilder
        implements PrimitiveOpBuilder<Long, IntegerFuncExpr> {

    @Override
    public Function<Object[], Long> toFunction(IntegerFuncExpr expr) {
        return expr::applyAsLong;
    }

    @Override
    public IntegerFuncExpr load(String name, int slot) {
        return args -> (long) args[slot];
    }

    @Override
    public IntegerFuncExpr constant(Long literal) {
        long primitive = literal;
        return args -> primitive;
    }

    @Override
    public IntegerFuncExpr add(IntegerFuncExpr leftOperand,
                               IntegerFuncExpr rightOperand) {
        return args -> (leftOperand.applyAsLong(args) +
                        rightOperand.applyAsLong(args));
    }

    @Override
    public IntegerFuncExpr sub(IntegerFuncExpr leftOperand,
                               IntegerFuncExpr rightOperand) {
        return args -> (leftOperand.applyAsLong(args) -
                        rightOperand.applyAsLong(args));
    }

    @Override
    public IntegerFuncExpr mul(IntegerFuncExpr leftOperand,
                               IntegerFuncExpr rightOperand) {
        return args -> (leftOperand.applyAsLong(args) *
                        rightOperand.applyAsLong(args));
    }

    @Override
    public IntegerFuncExpr div(IntegerFuncExpr leftOperand,
                               IntegerFuncExpr rightOperand) {
        return args -> (leftOperand.applyAsLong(args) /
                        rightOperand.applyAsLong(args));
    }

    @Override
    public IntegerFuncExpr pow(IntegerFuncExpr leftOperand,
                               IntegerFuncExpr rightOperand) {
        return args -> (long) Math.pow(leftOperand.applyAsLong(args),
                rightOperand.applyAsLong(args));
    }

    @Override
    public IntegerFuncExpr neg(IntegerFuncExpr operand) {
        return args -> -operand.applyAsLong(args);
    }

}

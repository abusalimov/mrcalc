package com.abusalimov.mrcalc.compile.impl.function;

import com.abusalimov.mrcalc.compile.exprtree.ExprBuilder;

/**
 * @author Eldar Abusalimov
 */
public class IntegerFuncExprBuilder
        implements ExprBuilder<Long, IntegerFuncExpr> {

    @Override
    public IntegerFuncExpr load(String name, int slot) {
        return args -> (Long) args[slot];
    }

    @Override
    public IntegerFuncExpr constant(Long literal) {
        return args -> literal;
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

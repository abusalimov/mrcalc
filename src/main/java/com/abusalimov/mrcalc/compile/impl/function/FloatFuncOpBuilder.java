package com.abusalimov.mrcalc.compile.impl.function;

import com.abusalimov.mrcalc.compile.exprtree.PrimitiveOpBuilder;

/**
 * @author Eldar Abusalimov
 */
public class FloatFuncOpBuilder
        implements PrimitiveOpBuilder<Double, FloatFuncExpr> {

    @Override
    public FloatFuncExpr load(String name, int slot) {
        return args -> (Double) args[slot];
    }

    @Override
    public FloatFuncExpr constant(Double literal) {
        return args -> literal;
    }

    @Override
    public FloatFuncExpr add(FloatFuncExpr leftOperand,
                             FloatFuncExpr rightOperand) {
        return args -> (leftOperand.applyAsDouble(args) +
                        rightOperand.applyAsDouble(args));
    }

    @Override
    public FloatFuncExpr sub(FloatFuncExpr leftOperand,
                             FloatFuncExpr rightOperand) {
        return args -> (leftOperand.applyAsDouble(args) -
                        rightOperand.applyAsDouble(args));
    }

    @Override
    public FloatFuncExpr mul(FloatFuncExpr leftOperand,
                             FloatFuncExpr rightOperand) {
        return args -> (leftOperand.applyAsDouble(args) *
                        rightOperand.applyAsDouble(args));
    }

    @Override
    public FloatFuncExpr div(FloatFuncExpr leftOperand,
                             FloatFuncExpr rightOperand) {
        return args -> (leftOperand.applyAsDouble(args) /
                        rightOperand.applyAsDouble(args));
    }

    @Override
    public FloatFuncExpr pow(FloatFuncExpr leftOperand,
                             FloatFuncExpr rightOperand) {
        return args -> Math.pow(leftOperand.applyAsDouble(args),
                rightOperand.applyAsDouble(args));
    }

    @Override
    public FloatFuncExpr neg(FloatFuncExpr operand) {
        return args -> -operand.applyAsDouble(args);
    }

}

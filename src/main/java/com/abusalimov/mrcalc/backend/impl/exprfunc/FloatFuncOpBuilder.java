package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.PrimitiveOpBuilder;

import java.util.function.Function;

/**
 * @author Eldar Abusalimov
 */
public class FloatFuncOpBuilder
        implements PrimitiveOpBuilder<Double, FloatFuncExpr> {

    @Override
    public Function<Object[], Double> toFunction(FloatFuncExpr expr) {
        return expr::applyAsDouble;
    }

    @Override
    public FloatFuncExpr load(String name, int slot) {
        return args -> (double) args[slot];
    }

    @Override
    public FloatFuncExpr constant(Double literal) {
        double primitive = literal;
        return args -> primitive;
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

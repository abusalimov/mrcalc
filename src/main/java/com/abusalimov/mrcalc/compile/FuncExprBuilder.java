package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.compile.exprtree.Expr;
import com.abusalimov.mrcalc.compile.exprtree.FloatExpr;
import com.abusalimov.mrcalc.compile.exprtree.IntegerExpr;

import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;

/**
 * @author Eldar Abusalimov
 */
public class FuncExprBuilder
        implements ExprBuilder<FuncExprBuilder.IntegerFunc, FuncExprBuilder.FloatFunc> {

    @Override
    public Expr invalid() {
        return Expr.INVALID;
    }

    @Override
    public IntegerFunc integerLoad(String name, int slot) {
        return args -> (Long) args[slot];
    }

    @Override
    public IntegerFunc integerConst(long literal) {
        return args -> literal;
    }

    @Override
    public IntegerFunc integerAdd(IntegerFunc leftOperand,
                                  IntegerFunc rightOperand) {
        return args -> (leftOperand.applyAsLong(args) +
                        rightOperand.applyAsLong(args));
    }

    @Override
    public IntegerFunc integerSub(IntegerFunc leftOperand,
                                  IntegerFunc rightOperand) {
        return args -> (leftOperand.applyAsLong(args) -
                        rightOperand.applyAsLong(args));
    }

    @Override
    public IntegerFunc integerMul(IntegerFunc leftOperand,
                                  IntegerFunc rightOperand) {
        return args -> (leftOperand.applyAsLong(args) *
                        rightOperand.applyAsLong(args));
    }

    @Override
    public IntegerFunc integerDiv(IntegerFunc leftOperand,
                                  IntegerFunc rightOperand) {
        return args -> (leftOperand.applyAsLong(args) /
                        rightOperand.applyAsLong(args));
    }

    @Override
    public IntegerFunc integerPow(IntegerFunc leftOperand,
                                  IntegerFunc rightOperand) {
        return args -> (long) Math.pow(leftOperand.applyAsLong(args),
                rightOperand.applyAsLong(args));
    }

    @Override
    public IntegerFunc integerNeg(IntegerFunc operand) {
        return args -> -operand.applyAsLong(args);
    }


    @Override
    public FloatFunc floatLoad(String name, int slot) {
        return args -> (Double) args[slot];
    }

    @Override
    public FloatFunc floatConst(double literal) {
        return args -> literal;
    }

    @Override
    public FloatFunc floatAdd(FloatFunc leftOperand,
                              FloatFunc rightOperand) {
        return args -> (leftOperand.applyAsDouble(args) +
                        rightOperand.applyAsDouble(args));
    }

    @Override
    public FloatFunc floatSub(FloatFunc leftOperand,
                              FloatFunc rightOperand) {
        return args -> (leftOperand.applyAsDouble(args) -
                        rightOperand.applyAsDouble(args));
    }

    @Override
    public FloatFunc floatMul(FloatFunc leftOperand,
                              FloatFunc rightOperand) {
        return args -> (leftOperand.applyAsDouble(args) *
                        rightOperand.applyAsDouble(args));
    }

    @Override
    public FloatFunc floatDiv(FloatFunc leftOperand,
                              FloatFunc rightOperand) {
        return args -> (leftOperand.applyAsDouble(args) /
                        rightOperand.applyAsDouble(args));
    }

    @Override
    public FloatFunc floatPow(FloatFunc leftOperand,
                              FloatFunc rightOperand) {
        return args -> Math.pow(leftOperand.applyAsDouble(args),
                rightOperand.applyAsDouble(args));
    }

    @Override
    public FloatFunc floatNeg(FloatFunc operand) {
        return args -> -operand.applyAsDouble(args);
    }

    @Override
    public IntegerFunc integerFromFloat(FloatFunc expr) {
        return args -> (long) expr.applyAsDouble(args);
    }

    @Override
    public FloatFunc floatFromInteger(IntegerFunc expr) {
        return args -> (double) expr.applyAsLong(args);
    }

    interface Func<T extends Number> extends Expr<T> {
    }

    interface IntegerFunc extends Func<Long>, IntegerExpr, ToLongFunction<Object[]> {
    }

    interface FloatFunc extends Func<Double>, FloatExpr, ToDoubleFunction<Object[]> {
    }
}

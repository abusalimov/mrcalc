package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.NumberMath;

/**
 * Implements numeric math on boxed {@link Double}s.
 *
 * @author Eldar Abusalimov
 */
public class DoubleFuncNumberMath extends FuncObjectMath<Double>
        implements NumberMath<Double, FuncExpr<Double>> {
    public static final DoubleFuncNumberMath INSTANCE = new DoubleFuncNumberMath();

    @Override
    public FuncExpr<Double> add(FuncExpr<Double> leftOperand, FuncExpr<Double> rightOperand) {
        return (runtime, args) -> leftOperand.eval(runtime, args) + rightOperand.eval(runtime, args);
    }

    @Override
    public FuncExpr<Double> sub(FuncExpr<Double> leftOperand, FuncExpr<Double> rightOperand) {
        return (runtime, args) -> leftOperand.eval(runtime, args) - rightOperand.eval(runtime, args);
    }

    @Override
    public FuncExpr<Double> mul(FuncExpr<Double> leftOperand, FuncExpr<Double> rightOperand) {
        return (runtime, args) -> leftOperand.eval(runtime, args) * rightOperand.eval(runtime, args);
    }

    @Override
    public FuncExpr<Double> div(FuncExpr<Double> leftOperand, FuncExpr<Double> rightOperand) {
        return (runtime, args) -> leftOperand.eval(runtime, args) / rightOperand.eval(runtime, args);
    }

    @Override
    public FuncExpr<Double> pow(FuncExpr<Double> leftOperand, FuncExpr<Double> rightOperand) {
        return (runtime, args) -> Math.pow(leftOperand.eval(runtime, args), rightOperand.eval(runtime, args));
    }

    @Override
    public FuncExpr<Double> neg(FuncExpr<Double> operand) {
        return (runtime, args) -> -operand.eval(runtime, args);
    }
}

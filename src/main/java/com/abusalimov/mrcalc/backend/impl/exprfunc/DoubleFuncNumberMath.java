package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.NumberMath;
import com.abusalimov.mrcalc.runtime.Runtime;

/**
 * Implements numeric math on boxed {@link Double}s.
 *
 * @author Eldar Abusalimov
 */
public class DoubleFuncNumberMath extends AbstractFuncNumberMath<Double>
        implements NumberMath<Double, Func<Double>> {
    public static final DoubleFuncNumberMath INSTANCE = new DoubleFuncNumberMath();

    @Override
    public Func<Double> add(Func<Double> leftOperand, Func<Double> rightOperand) {
        return (runtime, args) -> leftOperand.eval(runtime, args) + rightOperand.eval(runtime, args);
    }

    @Override
    public Func<Double> sub(Func<Double> leftOperand, Func<Double> rightOperand) {
        return (runtime, args) -> leftOperand.eval(runtime, args) - rightOperand.eval(runtime, args);
    }

    @Override
    public Func<Double> mul(Func<Double> leftOperand, Func<Double> rightOperand) {
        return (runtime, args) -> leftOperand.eval(runtime, args) * rightOperand.eval(runtime, args);
    }

    @Override
    public Func<Double> div(Func<Double> leftOperand, Func<Double> rightOperand) {
        return (runtime, args) -> leftOperand.eval(runtime, args) / rightOperand.eval(runtime, args);
    }

    @Override
    public Func<Double> pow(Func<Double> leftOperand, Func<Double> rightOperand) {
        return (runtime, args) -> Runtime.Util
                .powDouble(leftOperand.eval(runtime, args), rightOperand.eval(runtime, args));
    }

    @Override
    public Func<Double> neg(Func<Double> operand) {
        return (runtime, args) -> -operand.eval(runtime, args);
    }
}

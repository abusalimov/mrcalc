package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.NumberMath;
import com.abusalimov.mrcalc.runtime.Sequence;

/**
 * Implements numeric math on boxed {@link Double}s.
 *
 * @author Eldar Abusalimov
 */
public class DoubleFuncNumberMath extends FuncObjectMath<Double>
        implements NumberMath<Double, FuncExpr<Double>, FuncExpr<Sequence<?>>> {
    public static final DoubleFuncNumberMath INSTANCE = new DoubleFuncNumberMath();

    @Override
    public FuncExpr<Sequence<?>> range(FuncExpr<Double> startOperand, FuncExpr<Double> endOperand) {
        return (runtime, args) -> {
            throw new UnsupportedOperationException("Ranges must be constructed with integers bounds");
        };
    }

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

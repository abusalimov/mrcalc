package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.NumberMath;
import com.abusalimov.mrcalc.runtime.Runtime;

/**
 * Implements numeric math on boxed {@link Long}s.
 *
 * @author Eldar Abusalimov
 */
public class LongFuncNumberMath extends AbstractFuncNumberMath<Long>
        implements NumberMath<Long, Func<Long>> {
    public static final LongFuncNumberMath INSTANCE = new LongFuncNumberMath();

    @Override
    public Func<Long> add(Func<Long> leftOperand, Func<Long> rightOperand) {
        return (runtime, args) -> leftOperand.eval(runtime, args) + rightOperand.eval(runtime, args);
    }

    @Override
    public Func<Long> sub(Func<Long> leftOperand, Func<Long> rightOperand) {
        return (runtime, args) -> leftOperand.eval(runtime, args) - rightOperand.eval(runtime, args);
    }

    @Override
    public Func<Long> mul(Func<Long> leftOperand, Func<Long> rightOperand) {
        return (runtime, args) -> leftOperand.eval(runtime, args) * rightOperand.eval(runtime, args);
    }

    @Override
    public Func<Long> div(Func<Long> leftOperand, Func<Long> rightOperand) {
        return (runtime, args) -> leftOperand.eval(runtime, args) / rightOperand.eval(runtime, args);
    }

    @Override
    public Func<Long> pow(Func<Long> leftOperand, Func<Long> rightOperand) {
        return (runtime, args) -> Runtime.Util
                .powLong(leftOperand.eval(runtime, args), rightOperand.eval(runtime, args));
    }

    @Override
    public Func<Long> neg(Func<Long> operand) {
        return (runtime, args) -> -operand.eval(runtime, args);
    }
}

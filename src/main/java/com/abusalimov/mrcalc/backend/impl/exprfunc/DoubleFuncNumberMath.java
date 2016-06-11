package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.NumberMath;

import java.util.List;

/**
 * @author Eldar Abusalimov
 */
public class DoubleFuncNumberMath extends FuncObjectMath<Double> implements NumberMath<Double, FuncObjectExpr<Double>, FuncObjectExpr<List<?>>> {
    public static final DoubleFuncNumberMath INSTANCE = new DoubleFuncNumberMath();

    @Override
    public FuncObjectExpr<List<?>> range(FuncObjectExpr<Double> startOperand, FuncObjectExpr<Double> endOperand) {
        return args -> {
            throw new UnsupportedOperationException("Ranges must be constructed with integers bounds");
        };
    }

    @Override
    public FuncObjectExpr<Double> add(FuncObjectExpr<Double> leftOperand, FuncObjectExpr<Double> rightOperand) {
        return args -> leftOperand.apply(args) + rightOperand.apply(args);
    }

    @Override
    public FuncObjectExpr<Double> sub(FuncObjectExpr<Double> leftOperand, FuncObjectExpr<Double> rightOperand) {
        return args -> leftOperand.apply(args) - rightOperand.apply(args);
    }

    @Override
    public FuncObjectExpr<Double> mul(FuncObjectExpr<Double> leftOperand, FuncObjectExpr<Double> rightOperand) {
        return args -> leftOperand.apply(args) * rightOperand.apply(args);
    }

    @Override
    public FuncObjectExpr<Double> div(FuncObjectExpr<Double> leftOperand, FuncObjectExpr<Double> rightOperand) {
        return args -> leftOperand.apply(args) / rightOperand.apply(args);
    }

    @Override
    public FuncObjectExpr<Double> pow(FuncObjectExpr<Double> leftOperand, FuncObjectExpr<Double> rightOperand) {
        return args -> Math.pow(leftOperand.apply(args), rightOperand.apply(args));
    }

    @Override
    public FuncObjectExpr<Double> neg(FuncObjectExpr<Double> operand) {
        return args -> -operand.apply(args);
    }
}

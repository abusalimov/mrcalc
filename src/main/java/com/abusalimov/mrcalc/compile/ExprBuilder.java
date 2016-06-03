package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.compile.exprtree.Expr;
import com.abusalimov.mrcalc.compile.exprtree.FloatExpr;
import com.abusalimov.mrcalc.compile.exprtree.IntegerExpr;

/**
 * @author Eldar Abusalimov
 */
public interface ExprBuilder<I extends IntegerExpr, F extends FloatExpr> {

    Expr invalid();

    I integerLoad(String name, int slot);
    I integerConst(long literal);
    I integerAdd(I leftOperand, I rightOperand);
    I integerSub(I leftOperand, I rightOperand);
    I integerMul(I leftOperand, I rightOperand);
    I integerDiv(I leftOperand, I rightOperand);
    I integerPow(I leftOperand, I rightOperand);
    I integerNeg(I operand);

    F floatLoad(String name, int slot);
    F floatConst(double literal);
    F floatAdd(F leftOperand, F rightOperand);
    F floatSub(F leftOperand, F rightOperand);
    F floatMul(F leftOperand, F rightOperand);
    F floatDiv(F leftOperand, F rightOperand);
    F floatPow(F leftOperand, F rightOperand);
    F floatNeg(F operand);

    I integerFromFloat(F expr);
    F floatFromInteger(I expr);
}

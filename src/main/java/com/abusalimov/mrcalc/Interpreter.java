package com.abusalimov.mrcalc;

import com.abusalimov.mrcalc.ast.NodeVisitor;
import com.abusalimov.mrcalc.ast.expr.BinaryOpNode;
import com.abusalimov.mrcalc.ast.expr.LiteralNode;
import com.abusalimov.mrcalc.ast.expr.UnaryOpNode;
import com.abusalimov.mrcalc.compile.Code;

import java.math.BigInteger;

/**
 * @author Eldar Abusalimov
 */
public class Interpreter implements NodeVisitor<Long> {
    @Override
    public Long doVisit(LiteralNode node) {
        return (Long) node.getValue();
    }

    @Override
    public Long doVisit(UnaryOpNode node) {
        Long operand = visit(node.getOperand());
        switch (node.getOp()) {
            case PLUS:
                return operand;
            case MINUS:
                return -operand;
            default:
                throw new RuntimeException("Unknown sign " + node.getOp());
        }
    }

    @Override
    public Long doVisit(BinaryOpNode node) {
        Long a = visit(node.getOperandA());
        Long b = visit(node.getOperandB());
        switch (node.getOp()) {
            case ADD:
                return a + b;
            case SUB:
                return a - b;
            case MUL:
                return a * b;
            case DIV:
                return a / b;
            case POW:
                return BigInteger.valueOf(a)
                        .modPow(BigInteger.valueOf(b), BigInteger.ONE.shiftLeft(Long.SIZE))
                        .longValue();
            default:
                throw new RuntimeException("Unknown op " + node.getOp());
        }
    }

    public Long eval(Code code) {
        return visit(code.getExprNode());
    }
}
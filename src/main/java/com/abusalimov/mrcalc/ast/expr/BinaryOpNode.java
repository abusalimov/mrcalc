package com.abusalimov.mrcalc.ast.expr;

import com.abusalimov.mrcalc.ast.AbstractNode;
import com.abusalimov.mrcalc.ast.NodeArgVisitor;
import com.abusalimov.mrcalc.ast.NodeVisitor;

import java.util.Arrays;
import java.util.List;

/**
 * @author Eldar Abusalimov
 */
public class BinaryOpNode extends AbstractNode implements ExprNode {
    private Op op;
    private ExprNode operandA;
    private ExprNode operandB;

    public BinaryOpNode() {
    }

    public BinaryOpNode(Op op) {
        this(op, null, null);
    }

    public BinaryOpNode(Op op, ExprNode operandA, ExprNode operandB) {
        this.op = op;
        this.operandA = operandA;
        this.operandB = operandB;
    }

    public Op getOp() {
        return op;
    }

    public void setOp(Op op) {
        this.op = op;
    }

    public ExprNode getOperandA() {
        return operandA;
    }

    public void setOperandA(ExprNode operandA) {
        this.operandA = operandA;
    }

    public ExprNode getOperandB() {
        return operandB;
    }

    public void setOperandB(ExprNode operandB) {
        this.operandB = operandB;
    }

    @Override
    public List<? extends ExprNode> getChildren() {
        return Arrays.asList(operandA, operandB);
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.doVisit(this);
    }

    @Override
    public <T, A> T accept(NodeArgVisitor<T, A> visitor, A arg) {
        return visitor.doVisit(this, arg);
    }

    public enum Op {
        ADD("+"),
        SUB("-"),
        MUL("*"),
        DIV("/"),
        POW("^");

        private final String sign;

        Op(String sign) {
            this.sign = sign;
        }

        public static Op valueOfSign(String sign) {
            switch (sign) {
                case "+":
                    return ADD;
                case "-":
                    return SUB;
                case "*":
                    return MUL;
                case "/":
                    return DIV;
                case "^":
                    return POW;
                default:
                    throw new IllegalArgumentException("Unknown op " + sign);
            }
        }

        public String getSign() {
            return sign;
        }

        @Override
        public String toString() {
            return getSign();
        }
    }
}

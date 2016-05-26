package com.abusalimov.mrcalc.ast.expr;

import com.abusalimov.mrcalc.ast.AbstractNode;
import com.abusalimov.mrcalc.ast.Node;
import com.abusalimov.mrcalc.ast.NodeVisitor;

import java.util.Collections;
import java.util.List;

/**
 * @author Eldar Abusalimov
 */
public class UnaryOpNode extends AbstractNode implements ExprNode {
    private Op op;
    private ExprNode operand;

    public UnaryOpNode() {
    }

    public UnaryOpNode(Op op) {
        this(op, null);
    }

    public UnaryOpNode(Op op, ExprNode operand) {
        this.op = op;
        this.operand = operand;
    }

    public Op getOp() {
        return op;
    }

    public void setOp(Op op) {
        this.op = op;
    }

    public ExprNode getOperand() {
        return operand;
    }

    public void setOperand(ExprNode operand) {
        this.operand = operand;
    }

    @Override
    public List<? extends ExprNode> getChildren() {
        return Collections.singletonList(operand);
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.doVisit(this);
    }

    public enum Op {
        PLUS("+"),
        MINUS("-");

        private final String sign;

        Op(String sign) {
            this.sign = sign;
        }

        public static Op valueOfSign(String sign) {
            switch (sign) {
                case "+":
                    return PLUS;
                case "-":
                    return MINUS;
                default:
                    throw new IllegalArgumentException("Unknown sign " + sign);
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

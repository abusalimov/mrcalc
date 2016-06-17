package com.abusalimov.mrcalc.ast.expr;

import com.abusalimov.mrcalc.ast.*;

import java.util.Arrays;
import java.util.List;

/**
 * Reduce accepts a sequence and a neutral element and folds that using a binary lambda function.
 *
 * @author Eldar Abusalimov
 */
public class ReduceNode extends AbstractNode implements ExprNode {
    private ExprNode sequence;
    private ExprNode neutral;
    private LambdaNode lambda;

    public ReduceNode() {
    }

    public ReduceNode(ExprNode sequence, ExprNode neutral, LambdaNode lambda) {
        this.sequence = sequence;
        this.neutral = neutral;
        this.lambda = lambda;
    }

    public ExprNode getSequence() {
        return sequence;
    }

    public void setSequence(ExprNode sequence) {
        this.sequence = sequence;
    }

    public ExprNode getNeutral() {
        return neutral;
    }

    public void setNeutral(ExprNode neutral) {
        this.neutral = neutral;
    }

    public LambdaNode getLambda() {
        return lambda;
    }

    public void setLambda(LambdaNode lambda) {
        this.lambda = lambda;
    }

    @Override
    public List<? extends Node> getChildren() {
        return Arrays.asList(sequence, neutral, lambda);
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.doVisit(this);
    }

    @Override
    public <T, A> T accept(NodeArgVisitor<T, A> visitor, A arg) {
        return visitor.doVisit(this, arg);
    }
}

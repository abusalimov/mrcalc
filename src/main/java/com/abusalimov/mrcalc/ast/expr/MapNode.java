package com.abusalimov.mrcalc.ast.expr;

import com.abusalimov.mrcalc.ast.*;

import java.util.Arrays;
import java.util.List;

/**
 * Map accepts a sequence and transforms each element using an unary lambda function.
 *
 * @author Eldar Abusalimov
 */
public class MapNode extends AbstractNode implements ExprNode {
    private ExprNode sequence;
    private LambdaNode lambda;

    public MapNode() {
    }

    public MapNode(ExprNode sequence, LambdaNode lambda) {
        this.sequence = sequence;
        this.lambda = lambda;
    }

    public ExprNode getSequence() {
        return sequence;
    }

    public void setSequence(ExprNode sequence) {
        this.sequence = sequence;
    }

    public LambdaNode getLambda() {
        return lambda;
    }

    public void setLambda(LambdaNode lambda) {
        this.lambda = lambda;
    }

    @Override
    public List<? extends Node> getChildren() {
        return Arrays.asList(sequence, lambda);
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

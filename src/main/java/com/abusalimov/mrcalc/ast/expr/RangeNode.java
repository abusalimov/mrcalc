package com.abusalimov.mrcalc.ast.expr;

import com.abusalimov.mrcalc.ast.AbstractNode;
import com.abusalimov.mrcalc.ast.NodeArgVisitor;
import com.abusalimov.mrcalc.ast.NodeVisitor;

import java.util.Arrays;
import java.util.List;

/**
 * Range defines a sequence of integer numbers between {@link #getStart() start} and {@link
 * #getEnd() end} boundaries.
 *
 * @author Eldar Abusalimov
 */
public class RangeNode extends AbstractNode implements ExprNode {
    private ExprNode start;
    private ExprNode end;

    public RangeNode() {
    }

    public RangeNode(ExprNode start, ExprNode end) {
        this.start = start;
        this.end = end;
    }

    public ExprNode getStart() {
        return start;
    }

    public void setStart(ExprNode start) {
        this.start = start;
    }

    public ExprNode getEnd() {
        return end;
    }

    public void setEnd(ExprNode end) {
        this.end = end;
    }

    @Override
    public List<? extends ExprNode> getChildren() {
        return Arrays.asList(start, end);
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

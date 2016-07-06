package com.abusalimov.mrcalc.ast.stmt;

import com.abusalimov.mrcalc.ast.AbstractNode;
import com.abusalimov.mrcalc.ast.NodeArgVisitor;
import com.abusalimov.mrcalc.ast.NodeVisitor;

/**
 * The "out" statement just prints a given string.
 *
 * @author Eldar Abusalimov
 */
public class OutStmtNode extends AbstractNode implements StmtNode {
    private String string;

    public OutStmtNode() {
    }

    public OutStmtNode(String string) {
        this.string = string;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
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

package com.abusalimov.mrcalc.ast;

import com.abusalimov.mrcalc.ast.stmt.StmtNode;

import java.util.ArrayList;
import java.util.List;

/**
 * A program is defined as a sequence of statements.
 *
 * @author Eldar Abusalimov
 */
public class ProgramNode extends AbstractNode {
    private List<StmtNode> stmts;

    public ProgramNode() {
        stmts = new ArrayList<>();
    }

    public ProgramNode(List<StmtNode> stmts) {
        this.stmts = stmts;
    }

    public List<StmtNode> getStmts() {
        return stmts;
    }

    public void setStmts(List<StmtNode> stmts) {
        this.stmts = stmts;
    }

    @Override
    public List<? extends StmtNode> getChildren() {
        return stmts;
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

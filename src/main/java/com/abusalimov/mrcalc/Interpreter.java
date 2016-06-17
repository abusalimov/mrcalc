package com.abusalimov.mrcalc;

import com.abusalimov.mrcalc.ast.ProgramNode;
import com.abusalimov.mrcalc.compile.Stmt;
import com.abusalimov.mrcalc.compile.Variable;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The interpreter provides the necessary execution context for {@link #exec(List) running} compiled
 * {@link Stmt statements}.
 *
 * @author Eldar Abusalimov
 */
public class Interpreter {
    private Map<Variable, Object> memory = new HashMap<>();
    private PrintStream out;

    public Interpreter() {
    }

    public Interpreter(PrintStream out) {
        this.out = out;
    }

    public Object exec(Stmt... stmts) {
        return exec(Arrays.asList(stmts));
    }

    /**
     * Executes given statements and returns the result of the last one, if any.
     * <p>
     * The state is preserved between multiple executions, so that a statement can refer to a
     * variable initialized within some previous call. If an {@link #getOutStream() output} stream
     * is set, then results of executing `print` statements are printed out there.
     *
     * @param stmts a list of statements to execute, as returned by {@link
     *              com.abusalimov.mrcalc.compile.Compiler#compile(ProgramNode)}
     * @return the result of executing the last statement, if any {@code null} otherwise
     */
    public Object exec(List<Stmt> stmts) {
        Object result = null;

        for (Stmt stmt : stmts) {
            result = stmt.exec(memory);
            if (out != null && stmt.shouldPrintResult()) {
                String s;
                if (result instanceof long[]) {
                    s = Arrays.toString((long[]) result);
                } else {
                    s = result.toString();
                }
                out.println(s);
            }
        }

        return result;
    }

    public PrintStream getOutStream() {
        return out;
    }

    public void setOutStream(PrintStream out) {
        this.out = out;
    }
}

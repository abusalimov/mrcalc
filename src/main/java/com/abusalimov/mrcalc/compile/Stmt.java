package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.runtime.Runtime;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Stmt class represents executable code of a certain statement of a program.
 *
 * @author Eldar Abusalimov
 */
public class Stmt {
    private final BiFunction<Runtime, Object[], ?> exprFunction;
    private final List<Variable> inputVariables;
    private final Variable outputVariable;

    /**
     * Creates a new instance with given callable (which might be {@code null} for {@link #isComplete() incomplete
     * statements}), input and output variables.
     *
     * @param exprFunction   the callable to execute passing the input variable values to it
     * @param inputVariables the list of global variables referenced from within the statement
     * @param outputVariable the variable, to which to assign the result of calling the function
     */
    public Stmt(BiFunction<Runtime, Object[], ?> exprFunction, List<Variable> inputVariables, Variable outputVariable) {
        this.exprFunction = exprFunction;
        this.inputVariables = Objects.requireNonNull(inputVariables);
        this.outputVariable = Objects.requireNonNull(outputVariable);
    }

    /**
     * Executes the statement in a given context specified as the `memory` parameter and using the specified {@link
     * Runtime}. The result is then saved back into the memory and returned.
     *
     * @param runtime the {@link Runtime} to use
     * @param memory  the variables-to-values mapping representing the "memory"
     * @return the result
     */
    public Object exec(Runtime runtime, Map<Variable, Object> memory) {
        if (!isComplete()) {
            throw new UnsupportedOperationException("Incomplete statement");
        }
        Object[] args = bindVariables(memory);
        Object result = exprFunction.apply(runtime, args);
        memory.put(outputVariable, result);
        return result;
    }

    private Object[] bindVariables(Map<Variable, ?> memory) {
        Object[] ret = new Object[inputVariables.size()];
        int idx = 0;
        for (Variable variable : inputVariables) {
            Object value = memory.get(variable);
            if (value == null) {
                throw new UnboundVariableException(variable);
            }
            ret[idx++] = value;
        }
        return ret;
    }

    /**
     * Tells whether the statement was compiled successfully and can be {@link #exec(Runtime, Map) executed}. Normally,
     * incomplete statement are only live within the {@link Compiler} and not accessible from the outside.
     *
     * @return it the statement can be executed
     */
    public boolean isComplete() {
        return exprFunction != null;
    }

    public List<Variable> getInputVariables() {
        return Collections.unmodifiableList(inputVariables);
    }

    public Variable getOutputVariable() {
        return outputVariable;
    }

    public boolean shouldPrintResult() {
        return outputVariable.getName().startsWith("$print");  // TODO at least for now...
    }
}

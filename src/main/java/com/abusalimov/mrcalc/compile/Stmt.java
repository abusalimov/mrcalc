package com.abusalimov.mrcalc.compile;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Stmt class represents executable code of a certain statement of a program.
 *
 * @author Eldar Abusalimov
 */
public class Stmt {
    private final Function<Object[], ?> exprFunction;
    private final List<Variable> inputVariables;
    private final Variable outputVariable;

    public Stmt(Function<Object[], ?> exprFunction, List<Variable> inputVariables,
                Variable outputVariable) {
        this.exprFunction = Objects.requireNonNull(exprFunction);
        this.inputVariables = Objects.requireNonNull(inputVariables);
        this.outputVariable = Objects.requireNonNull(outputVariable);
    }

    /**
     * Executes the statement in a given context specified as the `memory` parameter. The result is
     * then saved back into the memory and returned.
     *
     * @param memory the variables-to-values mapping representing the "memory"
     * @return the result
     */
    public Object exec(Map<Variable, Object> memory) {
        Object[] args = bindVariables(memory);
        Object result = exprFunction.apply(args);
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

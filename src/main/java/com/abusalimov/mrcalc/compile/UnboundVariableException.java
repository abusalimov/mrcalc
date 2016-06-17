package com.abusalimov.mrcalc.compile;

/**
 * Runtime exception thrown in case a statement using a certain variable is executed prior to
 * the statement initializing that variable.
 *
 * @author Eldar Abusalimov
 */
public class UnboundVariableException extends RuntimeException {
    public UnboundVariableException() {
    }

    public UnboundVariableException(Variable variable) {
        this(variable.toString());
    }

    public UnboundVariableException(Variable variable, Throwable cause) {
        this(variable.toString(), cause);
    }

    public UnboundVariableException(String message) {
        super(message);
    }

    public UnboundVariableException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnboundVariableException(Throwable cause) {
        super(cause);
    }

    public UnboundVariableException(String message, Throwable cause, boolean enableSuppression,
                                    boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

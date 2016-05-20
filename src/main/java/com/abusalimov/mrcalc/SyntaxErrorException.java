package com.abusalimov.mrcalc;

/**
 * A syntax error is thrown to indicate that parser failed to recognize the input.
 *
 * @author Eldar Abusalimov
 */
public class SyntaxErrorException extends Exception {
    public SyntaxErrorException() {
    }

    public SyntaxErrorException(String message) {
        super(message);
    }

    public SyntaxErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public SyntaxErrorException(Throwable cause) {
        super(cause);
    }

}

package com.abusalimov.mrcalc.parse;

import com.abusalimov.mrcalc.diagnostic.Diagnostic;
import com.abusalimov.mrcalc.diagnostic.DiagnosticException;

import java.util.List;

/**
 * @author Eldar Abusalimov
 */
public class SyntaxErrorException extends DiagnosticException {
    public SyntaxErrorException(Diagnostic... diagnostic) {
        super(diagnostic);
    }

    public SyntaxErrorException(List<Diagnostic> diagnostics) {
        super(diagnostics);
    }

    public SyntaxErrorException(Throwable cause) {
        super(cause);
    }

    public SyntaxErrorException(Diagnostic diagnostic, Throwable cause) {
        super(diagnostic, cause);
    }

    public SyntaxErrorException(List<Diagnostic> diagnostics, Throwable cause) {
        super(diagnostics, cause);
    }
}

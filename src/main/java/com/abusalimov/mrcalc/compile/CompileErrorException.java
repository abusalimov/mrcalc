package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.diagnostic.Diagnostic;
import com.abusalimov.mrcalc.diagnostic.DiagnosticException;

import java.util.List;

/**
 * @author Eldar Abusalimov
 */
public class CompileErrorException extends DiagnosticException {
    public CompileErrorException(Diagnostic... diagnostic) {
        super(diagnostic);
    }

    public CompileErrorException(List<Diagnostic> diagnostics) {
        super(diagnostics);
    }

    public CompileErrorException(Throwable cause) {
        super(cause);
    }

    public CompileErrorException(Diagnostic diagnostic, Throwable cause) {
        super(diagnostic, cause);
    }

    public CompileErrorException(List<Diagnostic> diagnostics, Throwable cause) {
        super(diagnostics, cause);
    }
}

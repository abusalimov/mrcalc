package com.abusalimov.mrcalc.runtime;

import com.abusalimov.mrcalc.diagnostic.Diagnostic;
import com.abusalimov.mrcalc.diagnostic.DiagnosticException;

import java.util.List;

/**
 * Error that may occur in runtime.
 *
 * @author Eldar Abusalimov
 */
public class RuntimeErrorException extends DiagnosticException {
    public RuntimeErrorException(Diagnostic... diagnostic) {
        super(diagnostic);
    }

    public RuntimeErrorException(List<Diagnostic> diagnostics) {
        super(diagnostics);
    }

    public RuntimeErrorException(Throwable cause) {
        super(cause);
    }

    public RuntimeErrorException(Diagnostic diagnostic, Throwable cause) {
        super(diagnostic, cause);
    }

    public RuntimeErrorException(List<Diagnostic> diagnostics, Throwable cause) {
        super(diagnostics, cause);
    }
}

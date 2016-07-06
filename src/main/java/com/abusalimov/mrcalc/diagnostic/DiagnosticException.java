package com.abusalimov.mrcalc.diagnostic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * DiagnosticException wraps an immutable list of {@link Diagnostic}s.
 *
 * @author Eldar Abusalimov
 */
public class DiagnosticException extends Exception {
    private final List<Diagnostic> diagnostics;

    public DiagnosticException(Diagnostic... diagnostic) {
        this.diagnostics = Arrays.asList(diagnostic);
    }

    public DiagnosticException(List<Diagnostic> diagnostics) {
        this.diagnostics = new ArrayList<>(diagnostics);
    }

    public DiagnosticException(Throwable cause) {
        this();
        initCause(cause);
    }

    public DiagnosticException(Diagnostic diagnostic, Throwable cause) {
        this(diagnostic);
        initCause(cause);
    }

    public DiagnosticException(List<Diagnostic> diagnostics, Throwable cause) {
        this(diagnostics);
        initCause(cause);
    }

    public Diagnostic getFirstDiagnostic() {
        if (diagnostics.isEmpty()) {
            return null;
        }
        return diagnostics.get(0);
    }

    public List<Diagnostic> getDiagnostics() {
        return Collections.unmodifiableList(diagnostics);
    }

    @Override
    public String toString() {
        return String.format("%s {diagnostics=%s}", super.toString(), diagnostics);
    }
}

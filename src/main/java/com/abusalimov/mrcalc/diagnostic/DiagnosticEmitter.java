package com.abusalimov.mrcalc.diagnostic;

/**
 * Emits {@link Diagnostic}s to attached {@link DiagnosticListener}s.
 *
 * @author Eldar Abusalimov
 */
public interface DiagnosticEmitter {
    /**
     * Adds a new diagnostic listener.
     *
     * @param diagnosticListener a non-{@code null} listener instance
     */
    void addDiagnosticListener(DiagnosticListener diagnosticListener);

    /**
     * Removes a diagnostic listener, if any. No action is taken and no error is reported in case
     * the specified listener was not previously {@link #addDiagnosticListener (DiagnosticListener)
     * added}.
     *
     * @param diagnosticListener
     */
    void removeDiagnosticListener(DiagnosticListener diagnosticListener);
}

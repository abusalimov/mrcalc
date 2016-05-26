package com.abusalimov.mrcalc.diagnostic;

/**
 * {@link Diagnostic} receiver.
 *
 * @author Eldar Abusalimov
 */
public interface DiagnosticListener {
    /**
     * Called whenever a parser, compiler or interpreter encounters an error.
     *
     * @param diagnostic a non-{@code null} {@link Diagnostic} instance representing an issue to
     *                   report
     */
    void report(Diagnostic diagnostic);
}

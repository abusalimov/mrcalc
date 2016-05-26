package com.abusalimov.mrcalc.diagnostic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link DiagnosticListener listener} that stores all {@link Diagnostic}s into a list.
 *
 * @author Eldar Abusalimov
 */
public class DiagnosticCollector implements DiagnosticListener {
    private final List<Diagnostic> diagnostics;

    /**
     * Creates a new collector backed by a new {@link ArrayList}. The list can be retrieved by the
     * {@link #getDiagnostics()} method.
     */
    public DiagnosticCollector() {
        this(new ArrayList<>());
    }

    /**
     * Creates a new collector backed by the specified diagnostics list.
     *
     * @param diagnostics the list to store the reported diagnostics into
     */
    public DiagnosticCollector(List<Diagnostic> diagnostics) {
        this.diagnostics = diagnostics;
    }

    @Override
    public void report(Diagnostic diagnostic) {
        diagnostics.add(Objects.requireNonNull(diagnostic));
    }

    /**
     * Retrieves the list of the collected diagnostics.
     *
     * @return
     */
    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }
}

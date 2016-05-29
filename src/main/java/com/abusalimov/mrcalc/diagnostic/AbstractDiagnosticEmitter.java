package com.abusalimov.mrcalc.diagnostic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Eldar Abusalimov
 */
public class AbstractDiagnosticEmitter implements DiagnosticEmitter {
    private final List<DiagnosticListener> diagnosticListeners = new ArrayList<>();

    public void addDiagnosticListener(DiagnosticListener diagnosticListener) {
        diagnosticListeners.add(Objects.requireNonNull(diagnosticListener));
    }

    public void removeDiagnosticListener(DiagnosticListener diagnosticListener) {
        diagnosticListeners.remove(Objects.requireNonNull(diagnosticListener));
    }

    protected void emitDiagnostic(Diagnostic diagnostic) {
        for (DiagnosticListener listener : diagnosticListeners) {
            listener.report(diagnostic);
        }
    }
}

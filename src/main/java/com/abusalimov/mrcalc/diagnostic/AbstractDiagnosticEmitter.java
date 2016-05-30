package com.abusalimov.mrcalc.diagnostic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

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

    protected <E extends DiagnosticException> ListenerClosable<E> collectDiagnosticsToThrow(
            Function<List<Diagnostic>, E> e) {
        DiagnosticCollector diagnosticCollector = new DiagnosticCollector();
        addDiagnosticListener(diagnosticCollector);
        return () -> {
            removeDiagnosticListener(diagnosticCollector);

            List<Diagnostic> collectedDiagnostics = diagnosticCollector.getDiagnostics();
            if (collectedDiagnostics.size() > 0) {
                throw e.apply(collectedDiagnostics);
            }
        };
    }

    protected SilentListenerClosable collectDiagnostics(List<Diagnostic> diagnostics) {
        DiagnosticCollector diagnosticCollector = new DiagnosticCollector(diagnostics);
        addDiagnosticListener(diagnosticCollector);
        return () -> removeDiagnosticListener(diagnosticCollector);
    }

    public interface ListenerClosable<E extends DiagnosticException> extends AutoCloseable {
        @Override
        void close() throws E;
    }

    public interface SilentListenerClosable extends ListenerClosable {
        @Override
        void close();
    }
}

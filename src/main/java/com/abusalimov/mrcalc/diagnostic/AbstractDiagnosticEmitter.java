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

    public SilentListenerCloseable withDiagnosticListener(DiagnosticListener diagnosticListener) {
        return new DefaultListenerCloseable(this, diagnosticListener);
    }

    protected void emitDiagnostic(Diagnostic diagnostic) {
        for (DiagnosticListener listener : diagnosticListeners) {
            listener.report(diagnostic);
        }
    }

    protected <E extends DiagnosticException> DiagnosticCollectorCloseable<E> collectDiagnosticsToThrow(
            Function<List<Diagnostic>, E> exceptionConstructor) {
        return new DiagnosticCollectorCloseable<>(this, exceptionConstructor);
    }

    protected AutoCloseable collectDiagnostics(List<Diagnostic> diagnostics) {
        DiagnosticCollector diagnosticCollector = new DiagnosticCollector(diagnostics);
        return withDiagnosticListener(diagnosticCollector);
    }

    public abstract class AbstractListenerCloseable<L extends DiagnosticListener>
            implements ListenerCloseable {
        private final DiagnosticEmitter emitter;
        private final L listener;

        public AbstractListenerCloseable(DiagnosticEmitter emitter, L listener) {
            this.emitter = emitter;
            this.listener = listener;

            emitter.addDiagnosticListener(listener);
        }

        protected void safeClose() {
            emitter.removeDiagnosticListener(listener);
        }

        @Override
        public DiagnosticEmitter getDiagnosticEmitter() {
            return emitter;
        }

        @Override
        public L getDiagnosticListener() {
            return listener;
        }
    }

    public class DiagnosticCollectorCloseable<E extends DiagnosticException>
            extends AbstractListenerCloseable<DiagnosticCollector>
            implements DiagnosticListenerCloseable<E> {
        private final Function<List<Diagnostic>, E> exceptionConstructor;

        public DiagnosticCollectorCloseable(DiagnosticEmitter emitter,
                                            Function<List<Diagnostic>, E> exceptionConstructor) {
            super(emitter, new DiagnosticCollector());
            this.exceptionConstructor = exceptionConstructor;
        }

        @Override
        public void close() throws E {
            safeClose();

            List<Diagnostic> collectedDiagnostics = getDiagnosticListener().getDiagnostics();
            if (collectedDiagnostics.size() > 0) {
                throw createException(collectedDiagnostics);
            }
        }

        public E createException() {
            return createException(getDiagnosticListener().getDiagnostics());
        }

        public E createException(List<Diagnostic> diagnostics) {
            return exceptionConstructor.apply(diagnostics);
        }
    }

    public class DefaultListenerCloseable extends AbstractListenerCloseable<DiagnosticListener>
            implements SilentListenerCloseable {
        public DefaultListenerCloseable(DiagnosticEmitter emitter, DiagnosticListener listener) {
            super(emitter, listener);
        }

        @Override
        public void close() {
            safeClose();
        }
    }
}

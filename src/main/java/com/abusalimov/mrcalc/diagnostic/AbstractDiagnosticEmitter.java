package com.abusalimov.mrcalc.diagnostic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Provides basic implementation of the {@link DiagnosticEmitter} interface using a simple {@link ArrayList} of
 * listeners.
 *
 * @author Eldar Abusalimov
 */
public class AbstractDiagnosticEmitter implements DiagnosticEmitter {
    private final List<DiagnosticListener> diagnosticListeners = new ArrayList<>();

    @Override
    public void addDiagnosticListener(DiagnosticListener diagnosticListener) {
        diagnosticListeners.add(Objects.requireNonNull(diagnosticListener));
    }

    @Override
    public void removeDiagnosticListener(DiagnosticListener diagnosticListener) {
        diagnosticListeners.remove(Objects.requireNonNull(diagnosticListener));
    }

    @Override
    public SilentListenerCloseable withDiagnosticListener(DiagnosticListener diagnosticListener) {
        return new DefaultListenerCloseable(this, diagnosticListener);
    }

    /**
     * Sends the specified diagnostic to all the listeners attached.
     *
     * @param diagnostic the non-{@code null} {@link Diagnostic} instance to emit
     */
    protected void emitDiagnostic(Diagnostic diagnostic) {
        for (DiagnosticListener listener : diagnosticListeners) {
            listener.report(diagnostic);
        }
    }

    /**
     * Listens to diagnostics reported within the "try-with-resources" block and, if any collected, throws a new
     * exception created using the specified constructor function with the collected diagnostics.
     *
     * @param exceptionConstructor the function to call to create the exception in case there were diagnostics caught
     * @param <E>                  the exception type
     * @return the {@link AutoCloseable} to use with the "try-with-resources" statement
     */
    protected <E extends DiagnosticException> DiagnosticCollectorCloseable<E> collectDiagnosticsToThrow(
            Function<List<Diagnostic>, E> exceptionConstructor) {
        return new DiagnosticCollectorCloseable<>(this, exceptionConstructor);
    }

    /**
     * Collects diagnostics reported within the "try-with-resources" block adding them into the specified list.
     *
     * @param diagnostics the list to add the reported diagnostics to
     * @return the {@link AutoCloseable} to use with the "try-with-resources" statement
     */
    protected AutoCloseable collectDiagnostics(List<Diagnostic> diagnostics) {
        DiagnosticCollector diagnosticCollector = new DiagnosticCollector(diagnostics);
        return withDiagnosticListener(diagnosticCollector);
    }

    /**
     * Provides basic implementation of the {@link ListenerCloseable} interface.
     *
     * Subclasses still need to implement the {@link #close()} method, and are encouraged to reuse {@link #safeClose()}
     * for that.
     *
     * @param <L> the specific type of the listener maintained by this closeable
     */
    public abstract class AbstractListenerCloseable<L extends DiagnosticListener>
            implements ListenerCloseable {
        private final DiagnosticEmitter emitter;
        private final L listener;

        /**
         * Creates a new instance with the specified emitter and listener and attaches the latter to the former.
         *
         * @param emitter the emitter to attach the listener to
         * @param listener the listener to attach
         */
        public AbstractListenerCloseable(DiagnosticEmitter emitter, L listener) {
            this.emitter = emitter;
            this.listener = listener;

            emitter.addDiagnosticListener(listener);
        }

        /**
         * Removes the attached listener from the emitter.
         */
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

    /**
     * The {@link DiagnosticListenerCloseable} that uses a {@link DiagnosticCollector} instance as the listener and
     * throws the specified exception type upon {@link #close()} in case at least one diagnostic is reported.
     *
     * @param <E> the type of the exception that might be thrown on {@link #close()}
     */
    public class DiagnosticCollectorCloseable<E extends DiagnosticException>
            extends AbstractListenerCloseable<DiagnosticCollector>
            implements DiagnosticListenerCloseable<E> {
        private final Function<List<Diagnostic>, E> exceptionConstructor;

        /**
         * Creates a new instance for the specified emitter and attaches a new {@link DiagnosticCollector} instance to
         * that emitter.
         *
         * @param emitter the target emitter
         * @param exceptionConstructor the function to call to create the exception in case there were diagnostics caught
         */
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
                throw exceptionConstructor.apply(collectedDiagnostics);
            }
        }
    }

    /**
     * The default implementation of the {@link SilentListenerCloseable} interface.
     */
    public class DefaultListenerCloseable extends AbstractListenerCloseable<DiagnosticListener>
            implements SilentListenerCloseable {

        /**
         * Creates a new instance with the specified emitter and listener and attaches the latter to the former.
         *
         * @param emitter the emitter to attach the listener to
         * @param listener the listener to attach
         */
        public DefaultListenerCloseable(DiagnosticEmitter emitter, DiagnosticListener listener) {
            super(emitter, listener);
        }

        @Override
        public void close() {
            safeClose();
        }
    }
}

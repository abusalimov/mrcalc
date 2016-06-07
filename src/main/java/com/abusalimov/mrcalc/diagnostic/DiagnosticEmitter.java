package com.abusalimov.mrcalc.diagnostic;

import java.util.function.Supplier;

/**
 * Emits {@link Diagnostic}s to attached {@link DiagnosticListener}s.
 *
 * @author Eldar Abusalimov
 */
public interface DiagnosticEmitter {
    /**
     * Adds a new diagnostic listener.
     *
     * @param diagnosticListener a non-{@code null} listener instance to add
     */
    void addDiagnosticListener(DiagnosticListener diagnosticListener);

    /**
     * Removes a diagnostic listener, if any. No action is taken and no error is reported in case
     * the specified listener was not previously {@link #addDiagnosticListener (DiagnosticListener)
     * added}.
     *
     * @param diagnosticListener a non-{@code null} listener instance to remove, if any
     */
    void removeDiagnosticListener(DiagnosticListener diagnosticListener);

    /**
     * Adds a new diagnostic listener and returns an {@link AutoCloseable} that will remove that
     * listener upon cleanup.
     *
     * @param diagnosticListener a non-{@code null} listener instance to attach
     * @return a resource object to use within try-with-resources statement
     */
    SilentListenerCloseable withDiagnosticListener(DiagnosticListener diagnosticListener);

    default <R> R runWithDiagnosticListener(Supplier<R> function,
                                            DiagnosticListener diagnosticListener) {
        try (SilentListenerCloseable ignored =
                     withDiagnosticListener(diagnosticListener)) {
            return function.get();
        }
    }

    interface ListenerCloseable extends AutoCloseable {
        DiagnosticEmitter getDiagnosticEmitter();

        DiagnosticListener getDiagnosticListener();
    }

    interface DiagnosticListenerCloseable<E extends DiagnosticException> extends ListenerCloseable {
        @Override
        void close() throws E;
    }

    interface SilentListenerCloseable extends ListenerCloseable {
        @Override
        void close();
    }

}

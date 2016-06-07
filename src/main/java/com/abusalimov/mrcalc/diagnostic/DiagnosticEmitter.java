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
     * Adds a new diagnostic listener and returns an {@link AutoCloseable} that will remove that listener upon cleanup.
     *
     * @param diagnosticListener a non-{@code null} listener instance to attach
     * @return a resource object to use within try-with-resources statement
     */
    SilentListenerCloseable withDiagnosticListener(DiagnosticListener diagnosticListener);

    /**
     * Runs the specified function with a given {@link DiagnosticListener} attached.
     *
     * @param function           the function to run
     * @param diagnosticListener a non-{@code null} listener instance to attach
     * @param <R>                the return type of the function
     * @return the result of calling the function
     */
    default <R> R runWithDiagnosticListener(Supplier<R> function, DiagnosticListener diagnosticListener) {
        try (SilentListenerCloseable ignored = withDiagnosticListener(diagnosticListener)) {
            return function.get();
        }
    }

    /**
     * The {@link AutoCloseable} for use with the "try-with-resources" statement.
     */
    interface ListenerCloseable extends AutoCloseable {
        /**
         * Returns the {@link DiagnosticEmitter emitter} having the {@link DiagnosticListener listenter} attached.
         *
         * @return the non-{@code null} target emitter instance
         */
        DiagnosticEmitter getDiagnosticEmitter();

        /**
         * Returns the {@link DiagnosticListener listener} attached to the {@link DiagnosticEmitter emitter}, and which
         * will be detached upon leaving the "try-with-resources" block.
         *
         * @return the non-{@code null} listener instance
         */
        DiagnosticListener getDiagnosticListener();
    }

    /**
     * The {@link ListenerCloseable} that can only throw subclasses of the {@link DiagnosticException} class.
     *
     * @param <E> the type of exception thrown by the {@link #close()} method
     */
    interface DiagnosticListenerCloseable<E extends DiagnosticException> extends ListenerCloseable {
        @Override
        void close() throws E;
    }

    /**
     * The {@link ListenerCloseable} that doesn't throw any exception upon {@link #close()}.
     */
    interface SilentListenerCloseable extends ListenerCloseable {
        @Override
        void close();
    }

}

package com.abusalimov.mrcalc

import com.abusalimov.mrcalc.diagnostic.AbstractDiagnosticEmitter
import com.abusalimov.mrcalc.diagnostic.Diagnostic
import com.abusalimov.mrcalc.diagnostic.DiagnosticEmitter
import com.abusalimov.mrcalc.diagnostic.DiagnosticException

import java.util.function.Function
import java.util.function.Supplier

/**
 * Wraps a DiagnosticEmitter and provides a method for throwing a DiagnosticException in case the target emitter
 * report a diagnostic.
 *
 * @author Eldar Abusalimov
 */
class DiagnosticWrapper extends AbstractDiagnosticEmitter {
    DiagnosticEmitter wrapped

    DiagnosticWrapper(wrapped) {
        this.wrapped = wrapped
    }

    /**
     * Runs the closure with a diagnostic collector attached and throws an exception if at least one diagnostic was
     * reported.
     *
     * @param exceptionConstructor the construct for the exception that might be thrown
     * @param closure the code to run
     * @return the result of executing the closure
     */
    public <T> T runOrThrow(Function<List<Diagnostic>, DiagnosticException> exceptionConstructor, Closure<T> closure) {
        def diagnosticsToThrow = collectDiagnosticsToThrow(exceptionConstructor)
        try {
            return wrapped.runWithDiagnosticListener(closure as Supplier<T>, this.&emitDiagnostic)
        } finally {
            diagnosticsToThrow.close()
        }
    }
}

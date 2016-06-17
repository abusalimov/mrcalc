package com.abusalimov.mrcalc

import com.abusalimov.mrcalc.diagnostic.Diagnostic
import com.abusalimov.mrcalc.diagnostic.DiagnosticException
import groovy.test.GroovyAssert

import java.util.regex.Pattern

/**
 * Extends the standard set of assertions with methods for testing emitted Diagnostics.
 *
 * @author Eldar Abusalimov
 */
class DiagnosticAssert extends GroovyAssert {

    /**
     * Asserts that the given code closure throws a DiagnosticException of the specified type,
     * and that at least one Diagnostic attached to the exception contains the given substring.
     *
     * @param clazz the specific exception class
     * @param s the substring to search the diagnostic message for
     * @param code the closure to evaluate
     * @return the DiagnosticException thrown, if any, null otherwise
     */
    static DiagnosticException shouldDiagnose(Class<? extends DiagnosticException> clazz = DiagnosticException,
                                              String s, Closure code) {
        shouldDiagnose(clazz, [s], code)
    }

    /**
     * Asserts that the given code closure throws a DiagnosticException of the specified type,
     * and that at least one Diagnostic attached to the exception matches the given pattern.
     *
     * @param clazz the specific exception class
     * @param pat regex pattern to match the diagnostic against
     * @param code the closure to evaluate
     * @return the DiagnosticException thrown, if any, null otherwise
     */
    static DiagnosticException shouldDiagnose(Class<? extends DiagnosticException> clazz = DiagnosticException,
                                              Pattern pat, Closure code) {
        shouldDiagnose(clazz, [pat], code)
    }

    /**
     * Asserts that the given code closure throws a DiagnosticException of the specified type,
     * and that Diagnostics attached to the exception match every pattern given.
     *
     * @param clazz the specific exception class
     * @param patterns the list of string and/or regex pattern to test for
     * @param code the closure to evaluate
     * @return the DiagnosticException thrown, if any, null otherwise
     */
    static DiagnosticException shouldDiagnose(Class<? extends DiagnosticException> clazz = DiagnosticException,
                                              List<?> patterns, Closure code) {
        def th = shouldFail(clazz, code)

        if (th instanceof DiagnosticException) {
            List<Diagnostic> diagnostics = ((DiagnosticException) th).diagnostics
            patterns.each { matchAnyDiagnostic it, diagnostics }
            return th
        } else {
            return null
        }
    }

    private static void matchAnyDiagnostic(String s, List<Diagnostic> diagnostics) {
        matchAnyDiagnostic Pattern.compile(s, Pattern.LITERAL | Pattern.CASE_INSENSITIVE), diagnostics
    }

    private static void matchAnyDiagnostic(Pattern pat, List<Diagnostic> diagnostics) {
        if (!diagnostics.any { it.message =~ pat }) {
            fail "No diagnostic matched '${pat}': ${diagnostics}"
        }
    }

    private static void matchAnyDiagnostic(Object o, List<Diagnostic> diagnostics) {
        // God bless multiple dispatch
        throw new IllegalArgumentException("Expected String or Pattern")
    }
}

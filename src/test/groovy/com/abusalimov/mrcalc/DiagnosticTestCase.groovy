package com.abusalimov.mrcalc

import com.abusalimov.mrcalc.diagnostic.DiagnosticException

import java.util.regex.Pattern

/**
 * A JUnit 3 TestCase base class with helper methods for testing Diagnostics.
 *
 * @see DiagnosticAssert
 * @author Eldar Abusalimov
 */
abstract class DiagnosticTestCase extends GroovyTestCase {
    def diagnosticExceptionClass = DiagnosticException

    /**
     * @see DiagnosticAssert#shouldDiagnose(java.lang.Class, java.lang.String, groovy.lang.Closure)
     */
    DiagnosticException shouldDiagnose(Class<? extends DiagnosticException> clazz = getDiagnosticExceptionClass(),
                                       String s, Closure code) {
        DiagnosticAssert.shouldDiagnose(clazz, s, code)
    }

    /**
     * @see DiagnosticAssert#shouldDiagnose(java.lang.Class, java.util.regex.Pattern, groovy.lang.Closure)
     */
    DiagnosticException shouldDiagnose(Class<? extends DiagnosticException> clazz = getDiagnosticExceptionClass(),
                                       Pattern pat, Closure code) {
        DiagnosticAssert.shouldDiagnose(clazz, pat, code)
    }

    /**
     * @see DiagnosticAssert#shouldDiagnose(java.lang.Class, java.util.List, groovy.lang.Closure)
     */
    DiagnosticException shouldDiagnose(Class<? extends DiagnosticException> clazz = getDiagnosticExceptionClass(),
                                       List<?> patterns, Closure code) {
        DiagnosticAssert.shouldDiagnose(clazz, patterns, code)
    }
}

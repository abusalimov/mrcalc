package com.abusalimov.mrcalc

import com.abusalimov.mrcalc.compile.CompileErrorException
import com.abusalimov.mrcalc.compile.Compiler
import com.abusalimov.mrcalc.parse.Parser
import com.abusalimov.mrcalc.parse.impl.antlr.ANTLRParserImpl

/**
 * @author Eldar Abusalimov
 */
class CompilerTest extends DiagnosticTestCase {
    def diagnosticExceptionClass = CompileErrorException

    private Parser parser
    private Compiler compiler

    void setUp() {
        super.setUp()
        parser = new ANTLRParserImpl()
        compiler = new Compiler()
    }

    def compile(String s) {
        def node = parser.parse s
        compiler.compile node
    }

    void testCompilesExpressions() {
        assert null != compile("0")
        assert null != compile("(1)")
        assert null != compile("(1+2)")
        assert null != compile("1+2-3*4/5^6")
    }

    void testCompilesValidVarDefs() {
        assert null != compile("var answer = 42")
        assert null != compile("var x = 0; var y = 1; var z = 3")
    }

    void testCompilesValidVarRefs() {
        assert null != compile("var answer = 42; answer")
        assert null != compile("var x = 0; var y = 1; var z = 3; var foo = x+y+z")
    }

    void "test error on duplicate variables"() {
        shouldDiagnose("already defined") { compile("var answer = 42; var answer = -1") }
    }

    void "test error on undefined variables"() {
        shouldDiagnose("undefined variable") { compile("unknown") }
        shouldDiagnose("undefined variable") { compile("var x = y + z") }
        shouldDiagnose("undefined variable") { compile("var foo = bar; var bar = foo") }
    }

    void "test error on self-referencing variable"() {
        shouldDiagnose("undefined variable") { compile("var r = r") }
    }
}

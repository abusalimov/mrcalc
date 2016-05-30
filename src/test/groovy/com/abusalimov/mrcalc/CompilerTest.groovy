package com.abusalimov.mrcalc

import com.abusalimov.mrcalc.compile.CompileErrorException
import com.abusalimov.mrcalc.compile.Compiler
import com.abusalimov.mrcalc.parse.Parser
import com.abusalimov.mrcalc.parse.impl.antlr.ANTLRParserImpl

/**
 * @author Eldar Abusalimov
 */
class CompilerTest extends GroovyTestCase {
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

    void testThrowsErrorOnDuplicateVariable() {
        shouldFail CompileErrorException, { compile("var answer = 42; var answer = -1") }
    }
}

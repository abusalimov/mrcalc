package com.abusalimov.mrcalc

import com.abusalimov.mrcalc.compile.Compiler

/**
 * @author Eldar Abusalimov
 */
class CompilerTest extends GroovyTestCase {
    private Compiler compiler

    void setUp() {
        super.setUp()
        compiler = new Compiler()
    }

    def compile(String s) {
        compiler.compile s
    }

    void testParsesValidInput() {
        assert null != compile("0")
        assert null != compile("(1)")
        assert null != compile("(  (2 ))")
        assert null != compile(" ( ( ( 3 ) ) ) ")
    }

    void testThrowsSyntaxErrors() {
        shouldFail SyntaxErrorException, { compile "(" }
        shouldFail SyntaxErrorException, { compile ")" }
        shouldFail SyntaxErrorException, { compile "()" }
        shouldFail SyntaxErrorException, { compile "(-)" }
        shouldFail SyntaxErrorException, { compile "\$" }
        shouldFail SyntaxErrorException, { compile "sdf" }
        shouldFail SyntaxErrorException, { compile "((13)" }
        shouldFail SyntaxErrorException, { compile "(13))" }
    }
}

package com.abusalimov.mrcalc
/**
 * @author Eldar Abusalimov
 */
class InterpreterTest extends GroovyTestCase {
    private Interpreter interpreter

    void setUp() {
        super.setUp()
        interpreter = new Interpreter()
    }

    long eval(String s) {
        interpreter.eval s
    }

    void testEvalParsesParens() {
        assert 0L == eval("0")
        assert 1L == eval("(1)")
        assert 42L == eval(" ( 42 ) ")
    }

    void testThrowsSyntaxErrors() {
        shouldFail SyntaxErrorException, { eval "(" }
        shouldFail SyntaxErrorException, { eval ")" }
        shouldFail SyntaxErrorException, { eval "()" }
        shouldFail SyntaxErrorException, { eval "(-)" }
        shouldFail SyntaxErrorException, { eval "\$" }
        shouldFail SyntaxErrorException, { eval "sdf" }
        shouldFail SyntaxErrorException, { eval "((13)" }
        shouldFail SyntaxErrorException, { eval "(13))" }
    }
}

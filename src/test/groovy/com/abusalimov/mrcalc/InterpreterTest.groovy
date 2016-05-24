package com.abusalimov.mrcalc

import com.abusalimov.mrcalc.compile.Compiler

/**
 * @author Eldar Abusalimov
 */
class InterpreterTest extends GroovyTestCase {
    private Compiler compiler
    private Interpreter interpreter

    void setUp() {
        super.setUp()
        compiler = new Compiler()
        interpreter = new Interpreter()
    }

    long eval(String s) {
        def code = compiler.compile s
        interpreter.eval code
    }

    void testEvalParsesParens() {
        assert 0L == eval("0")
        assert 1L == eval("(1)")
        assert 42L == eval(" ( 42 ) ")
    }
}

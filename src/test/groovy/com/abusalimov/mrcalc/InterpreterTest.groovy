package com.abusalimov.mrcalc

import com.abusalimov.mrcalc.compile.Compiler
import com.abusalimov.mrcalc.parse.Parser
import com.abusalimov.mrcalc.parse.impl.antlr.ANTLRParserImpl

/**
 * @author Eldar Abusalimov
 */
class InterpreterTest extends GroovyTestCase {
    private Parser parser
    private Compiler compiler
    private Interpreter interpreter

    void setUp() {
        super.setUp()
        parser = new ANTLRParserImpl()
        compiler = new Compiler()
        interpreter = new Interpreter()
    }

    long eval(String s) {
        def node = parser.parse s
        def code = compiler.compile node
        interpreter.eval code
    }

    void testEvalParsesParens() {
        assert 0L == eval("0")
        assert 1L == eval("(1)")
        assert 42L == eval(" ( 42 ) ")
    }

    void testEvalCalculatesArithmetics() {
        assert 1L == eval("0 + 1")
        assert 27L == eval("(1+2)^3")
        assert 42L == eval("1 + 5*8 + 1")
        assert 54L == eval("(1+5) * (8+1)")
        shouldFail ArithmeticException, { eval "1/0" }
    }
}

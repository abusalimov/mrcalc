package com.abusalimov.mrcalc

import com.abusalimov.mrcalc.compile.Compiler
import com.abusalimov.mrcalc.parse.Parser
import com.abusalimov.mrcalc.parse.impl.antlr.ANTLRParserImpl
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail

/**
 * @author Eldar Abusalimov
 */
class InterpreterTest {
    private Parser parser
    private Compiler compiler
    private Interpreter interpreter

    @Before
    void setUp() {
        parser = new ANTLRParserImpl()
        compiler = new Compiler()
        interpreter = new Interpreter()
    }

    long eval(String s) {
        def node = parser.parse s
        def code = compiler.compile node
        interpreter.eval code
    }

    @Test
    void "evaluates literals"() {
        assert 0L == eval("0")
        assert 1L == eval("(1)")
        assert 42L == eval(" ( 42 ) ")
    }

    @Test
    void "calculates simple math expressions"() {
        assert 1L == eval("0 + 1")
        assert 27L == eval("(1+2)^3")
        assert 42L == eval("1 + 5*8 + 1")
        assert 54L == eval("(1+5) * (8+1)")
        shouldFail ArithmeticException, { eval "1/0" }
    }

    @Ignore("ExprVisitor stub")
    @Test
    void "can use variables"() {
        assert 1L == eval("var x = 1; x")
        assert 54L == eval("var six = 1 + 5; var nine = 8 + 1; six * nine")
    }

    @Ignore("ExprVisitor stub")
    @Test
    void "calculates variables lazily"() {
        assert 42L == eval("var fuuu = 1/0; 42")
    }
}

package com.abusalimov.mrcalc

import com.abusalimov.mrcalc.backend.Backend
import com.abusalimov.mrcalc.backend.impl.exprfunc.FuncBackendImpl
import com.abusalimov.mrcalc.compile.Compiler
import com.abusalimov.mrcalc.parse.Parser
import com.abusalimov.mrcalc.parse.impl.antlr.ANTLRParserImpl
import com.abusalimov.mrcalc.runtime.Runtime
import com.abusalimov.mrcalc.runtime.StreamRuntime
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail

/**
 * @author Eldar Abusalimov
 */
class InterpreterTest {
    private Parser parser
    private Backend backend
    private Compiler compiler
    private Runtime runtime
    private Interpreter interpreter

    @Before
    void setUp() {
        parser = new ANTLRParserImpl()
        backend = new FuncBackendImpl()
        compiler = new Compiler(backend)
        runtime = new StreamRuntime()
        interpreter = new Interpreter(runtime)
    }

    def eval(String s) {
        def node = parser.parse s
        def stmts = compiler.compile node
        interpreter.exec stmts
    }

    @Test
    void "evaluates literals"() {
        assert 0L == eval("0")
        assert 1L == eval("(1)")
        assert 42L == eval(" ((( 42 ))) ")
    }

    @Test
    void "calculates simple math expressions"() {
        assert 1L == eval("0 + 1")
        assert 27L == eval("(1+2)^3")
        assert 42L == eval("1 + 5*8 + 1")
        assert 54L == eval("(1+5) * (8+1)")
        assert 256L == eval("2^2^2^2")
        assert 256L == eval("(2^2)^2^2")
        assert 65536L == eval("2^(2^2^2)")
        shouldFail ArithmeticException, { eval "1/0" }
    }

    @Test
    void "can use variables"() {
        assert 1L == eval("var x = 1; x")
        assert 54L == eval("var six = 1 + 5; var nine = 8 + 1; six * nine")
    }

    @Test
    void "can compute map/reduce"() {
        assert [1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L] == eval("map({1,9}, x -> 1)")
        assert [1L, 4L, 9L, 16L, 25L, 36L, 49L, 64L, 81L] == eval("map({1,9}, x -> x*x)")
        assert 362880L == eval("reduce({1,9}, 1, x y -> x*y)")
        assert 0L == eval("reduce({1,999}, 0, x y -> x*y)")
        assert 0L == eval("reduce({1,999}, 0, x y -> x)")
        assert 999L == eval("reduce({1,999}, 0, x y -> y)")
//        assert 10000L == eval("""
//            var seq = map({0,9}, x -> map({0,9}, x -> map({0,9}, x -> map({0,9}, x -> 1))))
//            reduce(seq, 0, x y -> x + reduce(y, 0, x y -> x + reduce(y, 0, x y -> x + reduce(y, 0, x y -> x + y))))
//        """)
    }

    @Ignore("NIY")
    @Test
    void "calculates variables lazily"() {
        assert 42L == eval("var fuuu = 1/0; 42")
    }
}

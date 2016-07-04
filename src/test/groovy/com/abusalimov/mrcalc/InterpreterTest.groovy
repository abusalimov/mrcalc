package com.abusalimov.mrcalc

import com.abusalimov.mrcalc.backend.Backend
import com.abusalimov.mrcalc.backend.impl.bytebuddy.BytebuddyBackendImpl
import com.abusalimov.mrcalc.backend.impl.exprfunc.FuncBackendImpl
import com.abusalimov.mrcalc.compile.Compiler
import com.abusalimov.mrcalc.parse.Parser
import com.abusalimov.mrcalc.parse.impl.antlr.ANTLRParserImpl
import com.abusalimov.mrcalc.runtime.Runtime
import com.abusalimov.mrcalc.runtime.impl.stream.StreamRuntime
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

import static groovy.test.GroovyAssert.shouldFail
/**
 * Integration tests including tests for the Interpreter.
 *
 * @author Eldar Abusalimov
 */
@RunWith(Parameterized.class)
class InterpreterTest {
    private Parser parser
    private Compiler compiler
    private Backend backend
    private Runtime runtime = new StreamRuntime()
    private Interpreter interpreter

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        [[new FuncBackendImpl()] as Object[],
         [new BytebuddyBackendImpl<>()] as Object[]]
    }

    InterpreterTest(Backend backend) {
        this.backend = backend
    }

    @Before
    void setUp() {
        parser = new ANTLRParserImpl()
        compiler = new Compiler(backend)
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
    void "calculates simple integer math expressions"() {
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
    void "calculates simple math expressions with mixed types"() {
        assert 1D.isCloseTo(eval(".0 + 1"))
        assert 2D.isCloseTo(eval("(1+3)^.5"))
        assert 1.5D.isCloseTo(eval("(1+5)^-1.0 * (8+1)"))
        assert 4D.isCloseTo(eval("2^(2^2^.5)"))
        assert Double.POSITIVE_INFINITY == eval("1/.0")
    }

    @Test
    void "can use variables"() {
        assert 1L == eval("var x = 1; x")
        assert 54L == eval("var six = 1 + 5; var nine = 8 + 1; six * nine")
    }

    @Test
    void "can compute reduce"() {
        assert 362880L == eval("reduce({1,9}, 1, x y -> x*y)")
        assert 0L == eval("reduce({1,999}, 0, x y -> x*y)")
        assert 0L == eval("reduce({1,999}, 0, x y -> x)")
        assert 999L == eval("reduce({1,999}, 0, x y -> y)")

        assert 24L == eval("reduce({1,9}, 1, x y -> x*y) / reduce({5,9}, 1, x y -> x*y)")
        assert 24L == eval("reduce({1,4}, 1, x y -> x*y / reduce({1,1}, 1, x y -> x*y))")
    }

    @Test
    void "can compute map"() {
        assert [1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L] == eval("map({1,9}, x -> 1)")
        assert [1L, 4L, 9L, 16L, 25L, 36L, 49L, 64L, 81L] == eval("map({1,9}, x -> x*x)")

        assert [1D, 2D, 3D, 4D, 5D, 6D, 7D, 8D, 9D] == eval("var dSeq = map({1,9}, x -> x + .0)")
        assert [2D, 4D, 6D, 8D, 10D, 12D, 14D, 16D, 18D] == eval("map(dSeq, x -> x + x)")
    }

    @Test
    void "supports variables of any type"() {
        assert 2L == eval("var l = 1 + 1; l")
        assert 3D == eval("var d = l + 1; d")

        assert [0L, 1L, 2L] == eval("var lSeq = {0, l}; lSeq")
        assert [1D, 2D, 3D] == eval("var dSeq = map(lSeq, x -> x + 1.0); dSeq")
        assert [1D, 4D, 9D] == eval("map(dSeq, x -> x ^ 2)")

        assert [[], [0L], [0L, 1L, 2L, 3L]] == eval("var lSeqSeq = map(lSeq, x -> {0, x^2 - 1}); lSeqSeq")
        assert [[], [1D], [1D, 2D, 3D, 4D]] == eval("var dSeqSeq = map(lSeqSeq, xs -> map(xs, x -> x + 1.0)); dSeqSeq")

        assert [0L, 0L, 6L] == eval("var lSumSeq = map(lSeqSeq, xs -> reduce(xs, 0, x y -> x+y)); lSumSeq")
        assert [0D, 1D, 10D] == eval("var dSumSeq = map(dSeqSeq, xs -> reduce(xs, 0.0, x y -> x+y)); dSumSeq")

        assert 6L == eval("reduce(lSumSeq, 0, x y -> x+y)")
        assert 11D == eval("reduce(dSumSeq, 0.0, x y -> x+y)")

        assert [6L] == eval("reduce(lSeqSeq, {0, 0}, xs ys -> {" +
                "reduce(xs, 0, x y -> x+y) + reduce(ys, 0, x y -> x+y), " +
                "reduce(xs, 0, x y -> x+y) + reduce(ys, 0, x y -> x+y)})")
    }
}

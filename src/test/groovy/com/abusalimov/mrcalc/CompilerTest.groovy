package com.abusalimov.mrcalc

import com.abusalimov.mrcalc.backend.Backend
import com.abusalimov.mrcalc.backend.impl.exprfunc.FuncBackendImpl
import com.abusalimov.mrcalc.compile.CompileErrorException
import com.abusalimov.mrcalc.compile.Compiler
import com.abusalimov.mrcalc.parse.Parser
import com.abusalimov.mrcalc.parse.impl.antlr.ANTLRParserImpl
import org.junit.Before
import org.junit.Test
/**
 * @author Eldar Abusalimov
 */
class CompilerTest {
    def shouldDiagnose = DiagnosticAssert.&shouldDiagnose.curry CompileErrorException

    private Parser parser
    private Backend backend
    private Compiler compiler

    @Before
    void setUp() {
        parser = new ANTLRParserImpl()
        backend = new FuncBackendImpl()
        compiler = new Compiler(backend)
    }

    def compile(String s) {
        def node = parser.parse s
        compiler.compile node
    }

    @Test
    void "compiles simple math expressions"() {
        assert compile("0")
        assert compile("(1)")
        assert compile("(1+2)")
        assert compile("1+2-3*4/5^6")
    }

    @Test
    void "compiles valid variable definition statements"() {
        assert compile("var answer = 42")
        assert compile("var x = 0; var y = 1; var z = 3")
    }

    @Test
    void "compiles valid variable references"() {
        assert compile("var answer = 42; answer")
        assert compile("var x = 0; var y = 1; var z = 3; var foo = x+y+z")
    }

    @Test
    void "compiles lambdas within map/reduce expressions"() {
        assert compile("map({1,2}, a -> a^2)")
        assert compile("reduce({0,9}, 0, a b -> a+b)")
        assert compile("map({0,9}, x -> reduce({1,x}, 1, a b -> a * b))")
    }

    @Test
    void "reports error on duplicate variables"() {
        shouldDiagnose("already defined") { compile("var answer = 42; var answer = -1") }
    }

    @Test
    void "reports error on undefined variables"() {
        shouldDiagnose("undefined variable") { compile("unknown") }
        shouldDiagnose("undefined variable") { compile("var x = y + z") }
        shouldDiagnose("undefined variable") { compile("var foo = bar; var bar = foo") }
    }

    @Test
    void "reports error on self-referencing variable"() {
        shouldDiagnose("undefined variable") { compile("var r = r") }
    }
}

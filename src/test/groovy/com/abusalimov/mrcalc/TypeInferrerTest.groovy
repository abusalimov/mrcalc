package com.abusalimov.mrcalc

import com.abusalimov.mrcalc.ast.ExprHolderNode
import com.abusalimov.mrcalc.compile.CompileErrorException
import com.abusalimov.mrcalc.compile.TypeInferrer
import com.abusalimov.mrcalc.compile.Variable
import com.abusalimov.mrcalc.compile.type.PrimitiveType
import com.abusalimov.mrcalc.compile.type.SequenceType
import com.abusalimov.mrcalc.compile.type.Type
import com.abusalimov.mrcalc.parse.Parser
import com.abusalimov.mrcalc.parse.impl.antlr.ANTLRParserImpl
import org.junit.Before
import org.junit.Test

import java.util.function.Function

/**
 * @author Eldar Abusalimov
 */
class TypeInferrerTest {
    def shouldDiagnose = DiagnosticAssert.&shouldDiagnose.curry CompileErrorException

    private Parser parser
    private TypeInferrer typeInferrer
    private DiagnosticWrapper typeInferrerWrapper

    @Before
    void setUp() {
        parser = new ANTLRParserImpl()
        typeInferrer = new TypeInferrer()
        typeInferrerWrapper = new DiagnosticWrapper(typeInferrer)
    }

    Type infer(String s, Map<String, Type> variableTypeMap = Collections.emptyMap()) {
        def node = parser.parse s
        def variableMap = (Map<String, Variable>) variableTypeMap.collectEntries {
            name, type -> [name, new Variable(name, type)]
        }

        def lastStmt = node.stmts.last()
        if (!(lastStmt instanceof ExprHolderNode)) {
            throw new IllegalArgumentException("Not an ExprHolderNode instance")
        }

        typeInferrerWrapper.runOrThrow(CompileErrorException.metaClass.&invokeConstructor as Function) {
            typeInferrer.infer((ExprHolderNode) lastStmt, variableMap).exprType
        }
    }

    @Test
    void "infers direct type of literals"() {
        assert PrimitiveType.INTEGER == infer("0")
        assert PrimitiveType.INTEGER == infer("1")
        assert PrimitiveType.INTEGER == infer("(1+2)")
        assert PrimitiveType.FLOAT == infer("1.0")
    }

    @Test
    void "infers proper types from variable references"() {
        assert PrimitiveType.INTEGER == infer("foo", ["foo": PrimitiveType.INTEGER])
        assert PrimitiveType.FLOAT == infer("a^2", ["a": PrimitiveType.FLOAT])
    }

    @Test
    void "infers type of simple expressions"() {
        assert PrimitiveType.FLOAT == infer("0*0.0")
        assert PrimitiveType.FLOAT == infer("1.0+0")
        assert PrimitiveType.INTEGER == infer("1/2")
        assert PrimitiveType.FLOAT == infer("1./2")
        assert PrimitiveType.FLOAT == infer("1^(2.)")
        assert PrimitiveType.FLOAT == infer("foo+boo", ["foo": PrimitiveType.INTEGER, "boo": PrimitiveType.FLOAT])
        assert PrimitiveType.FLOAT == infer("foo+boo", ["foo": PrimitiveType.FLOAT, "boo": PrimitiveType.FLOAT])
        assert PrimitiveType.INTEGER == infer("foo+boo", ["foo": PrimitiveType.INTEGER, "boo": PrimitiveType.INTEGER])

        assert shouldDiagnose("cannot be applied") {
            infer("map({1,2}, x -> 1) + seq", ["seq": SequenceType.of(PrimitiveType.INTEGER)])
        }
        assert shouldDiagnose("cannot be applied") {
            infer("map({1,2}, x -> 1) + num", ["num": PrimitiveType.INTEGER])
        }
        assert shouldDiagnose("cannot be applied") {
            infer("10 + seq", ["seq": SequenceType.of(PrimitiveType.INTEGER)])
        }
    }

    @Test
    void "infers compatibility of neutral element and lambda expression"() {
        /* shouldn't diagnose "incompatible" */ infer("reduce({0,9}, 0, a b -> a+b)")
        /* shouldn't diagnose "incompatible" */ infer("reduce(map({0,9}, x -> {1,1}), {0,0}, a b -> {1,2})")
        assert shouldDiagnose("incompatible") { infer("reduce({0,9}, {0,0}, a b -> {1,2})") }
        assert shouldDiagnose("incompatible") { infer("reduce({0,9}, 0.0, a b -> a+b)") }
        assert shouldDiagnose("incompatible") { infer("reduce({0,9}, 0, a b -> 1.)") }
        assert shouldDiagnose("incompatible") { infer("reduce({0,9}, 0., a b -> 1)") }
        assert shouldDiagnose("incompatible") { infer("reduce({0,9}, 0., a b -> {1,2})") }
    }

    @Test
    void "infers resulting types of map/reduce expressions"() {
        def vars = ["ints"  : infer("map({1,2}, a -> a^2)"),
                    "floats": infer("map({1,2}, a -> a+2.0)")]

        assert SequenceType.of(PrimitiveType.INTEGER) == vars.ints
        assert SequenceType.of(PrimitiveType.FLOAT) == vars.floats
        assert PrimitiveType.INTEGER == infer("reduce(ints, 0, a b -> a+b)", vars)
        assert PrimitiveType.FLOAT == infer("reduce(floats, 0.0, a b -> a+b)", vars)
        assert shouldDiagnose("incompatible") { infer("reduce(ints, 0.0, a b -> a+b)", vars) }
        assert shouldDiagnose("incompatible") { infer("reduce(floats, 0, a b -> a+b)", vars) }
        assert PrimitiveType.FLOAT == infer("reduce(ints, 0, a b -> a+b) + reduce(floats, 0.0, a b -> a+b)", vars)
    }

    @Test
    void "infers resulting types of nested map/reduce expressions"() {
        def vars = ["ints"  : infer("map({1,2}, x -> map({1,2}, x -> map({1,2}, x -> map({1,2}, x -> 1))))"),
                    "floats": infer("map({1,2}, x -> map({1,2}, x -> map({1,2}, x -> map({1,2}, x -> 1.0))))")]

        assert SequenceType.of(SequenceType.of(SequenceType.of(SequenceType.of(PrimitiveType.INTEGER)))) == vars.ints
        assert SequenceType.of(SequenceType.of(SequenceType.of(SequenceType.of(PrimitiveType.FLOAT)))) == vars.floats
//        assert PrimitiveType.INTEGER == infer("reduce(ints, 0,  x y -> x + reduce(y, 0,  x y -> x + reduce(y, 0,  x y -> x + reduce(y, 0,  x y -> x + y))))", vars)
//        assert PrimitiveType.FLOAT == infer("reduce(ints,   0., x y -> x + reduce(y, 0., x y -> x + reduce(y, 0., x y -> x + reduce(y, 0., x y -> x + y))))", vars)
//        assert PrimitiveType.FLOAT == infer("reduce(floats, 0., x y -> x + reduce(y, 0., x y -> x + reduce(y, 0., x y -> x + reduce(y, 0., x y -> x + y))))", vars)
        assert shouldDiagnose("incompatible") {
            infer("reduce(floats, 0, x y -> x + reduce(y, 0, x y -> x + reduce(y, 0, x y -> x + reduce(y, 0, x y -> x + y))))", vars)
        }
    }

    @Test
    void "checks arity of lambdas in map/reduce expressions"() {
        assert shouldDiagnose("accepts exactly 1 arg") { infer("map({1,2}, -> 1)") }
        /* shouldn't diagnose "accepts exactly 1 arg" */ infer("map({1,2}, x -> 1)")
        assert shouldDiagnose("accepts exactly 1 arg") { infer("map({1,2}, x y -> 1)") }

        assert shouldDiagnose("accepts exactly 2 arg") { infer("reduce({1,2}, 0, -> 1)") }
        assert shouldDiagnose("accepts exactly 2 arg") { infer("reduce({1,2}, 0, z -> 1)") }
        /* shouldn't diagnose "accepts exactly 2 arg" */ infer("reduce({1,2}, 0, x y -> 1)")
        assert shouldDiagnose("accepts exactly 2 arg") { infer("reduce({1,2}, 0, x y z -> 1)") }
    }

    @Test
    void "checks lambda parameters for duplicates"() {
        assert shouldDiagnose("duplicate lambda parameter") { infer("map({1,2}, x x -> 1)") }
        assert shouldDiagnose("duplicate lambda parameter") { infer("map({1,2}, x x x -> 1)") }

        assert shouldDiagnose("duplicate lambda parameter") { infer("reduce({1,2}, 0, x x -> 1)") }
        assert shouldDiagnose("duplicate lambda parameter") { infer("reduce({1,2}, 0, x x x -> 1)") }
        assert shouldDiagnose("duplicate lambda parameter") { infer("reduce({1,2}, 0, y x x -> 1)") }
    }

    @Test
    void "checks sequence boundaries to be integers"() {
        assert shouldDiagnose("boundary") { infer("{1, 2.}") }
        /* shouldn't diagnose "boundary" */ infer("{9,0}")
        assert shouldDiagnose("boundary") { infer("{map({1,2}, x -> 1),2}") }
        assert shouldDiagnose("boundary") { infer("{reduce(map({1,2}, x -> 1.), 0., x y -> 1.), 2}") }
        /* shouldn't diagnose "boundary" */ infer("{reduce({1,2}, 0, x y -> 1), 2}")
    }

    @Test
    void "checks the first parameter of map/reduce to be a sequence"() {
        assert shouldDiagnose("cannot be applied to a scalar") { infer("reduce(1, 0., x y -> x+y)") }
        /* shouldn't diagnose "cannot be applied to a scalar" */ infer("reduce({0,9}, 0, x y -> x+y)")
        assert shouldDiagnose("cannot be applied to a scalar") { infer("map(1, x -> 1)") }
        /* shouldn't diagnose "cannot be applied to a scalar" */ infer("map({0,9}, x -> 1)")
    }

    @Test
    void "undefined variables"() {
        assert shouldDiagnose("undefined") { infer("a+b", ["aa": PrimitiveType.INTEGER, "bb": PrimitiveType.INTEGER]) }
        assert shouldDiagnose("undefined") { infer("map(a, x -> x)") }
        assert shouldDiagnose("undefined") { infer("reduce(a, 0., x y -> x+y)") }
        assert shouldDiagnose("undefined") { infer("map({1, 2}, x -> y)") }
        assert shouldDiagnose("undefined") { infer("map({1, 2}, x -> y)", ["y": PrimitiveType.INTEGER]) }
    }
}

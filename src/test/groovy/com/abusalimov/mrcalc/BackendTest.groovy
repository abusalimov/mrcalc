package com.abusalimov.mrcalc

import com.abusalimov.mrcalc.backend.Backend
import com.abusalimov.mrcalc.backend.Expr
import com.abusalimov.mrcalc.backend.NumberMath
import com.abusalimov.mrcalc.backend.impl.exprfunc.FuncBackendImpl
import com.abusalimov.mrcalc.runtime.Evaluable
import com.abusalimov.mrcalc.runtime.Runtime
import org.junit.Before
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail

/**
 * @author Eldar Abusalimov
 */
@SuppressWarnings("GroovyAssignabilityCheck")
class BackendTest<E extends Expr> {
    static {
        def runtime = new Runtime()
        Evaluable.metaClass.apply = { Object... args -> eval(runtime, args) }
        Evaluable.metaClass.call = { Object... args -> apply(args) }
    }

    private Backend<E> backend

    @Before
    void setUp() {
        backend = new FuncBackendImpl<>() as Backend<E>
    }

    def getNumberMath(Class<? extends Number> returnType) {
        backend.getNumberMath(returnType)
    }

    def getNumberCast(Class<? extends Number> toType, Class<? extends Number> fromType) {
        backend.getNumberCast(toType, fromType)
    }

    def getIntegerMath() {
        (NumberMath<Long, E, E>) getNumberMath(Long)
    }

    def getFloatMath() {
        (NumberMath<Double, E, E>) getNumberMath(Double)
    }

    def getL2d() {
        getNumberCast(Double, Long)
    }

    def getD2l() {
        getNumberCast(Long, Double)
    }

    def iLoad(int n) {
        integerMath.load(n)
    }
    def iConst(long l) {
        integerMath.constant(l)
    }

    def fLoad(int n) {
        floatMath.load(n)
    }
    def fConst(double d) {
        floatMath.constant(d)
    }

    @Test
    void "test constant"() {
        def constant = integerMath.constant(42L)
        def fn = integerMath.toFunction(constant)
        assert 42 == fn()
    }

    @Test
    void "test load variable"() {
        def load = integerMath.load(0)
        def fn = integerMath.toFunction(load)
        assert 0 == fn(0L)
        assert 7 == fn(7L)
    }

    @Test
    void "test expressions with variables"() {
        def iAdd = integerMath.toFunction(integerMath.add(iLoad(0), iLoad(1)))
        assert 7L == iAdd(3L, 4L)
        assert 150L == iAdd(100L, 50L)

        def iMul = integerMath.toFunction(integerMath.mul(iLoad(0), iConst(42)))
        assert 0L == iMul(0L)
        assert 294L == iMul(7L)

        def iPow = integerMath.toFunction(integerMath.pow(iLoad(0), iLoad(1)))
        assert 27L == iPow(3L, 3L)
        assert 32L == iPow(2L, 5L)
    }

    @Test
    void "test cast"() {
        shouldFail ClassCastException, { floatMath.toFunction(floatMath.mul(iConst(10), fConst(2))).apply() }
        assert 20.0d == integerMath.toFunction(floatMath.mul(l2d.cast(iConst(10)), fConst(2))).apply()

        shouldFail ClassCastException, { integerMath.toFunction(integerMath.mul(iConst(10), fConst(2))).apply() }
        assert 20L == integerMath.toFunction(integerMath.mul(iConst(10), d2l.cast(fConst(2)))).apply()
    }

    @Test
    void "test ranges"() {
        assert [0L, 1L, 2L, 3L] == integerMath.toFunction(integerMath.range(iConst(0), iConst(3))).apply()
        assert [] == integerMath.toFunction(integerMath.range(iConst(3), iConst(0))).apply()
        shouldFail ClassCastException, { integerMath.toFunction(integerMath.range(iConst(0), fConst(2))).apply() }
    }

    @Test
    void "test map/reduce"() {
        def iMap = integerMath.map(integerMath.range(iConst(0), iConst(3)), integerMath.pow(iLoad(0), iConst(2)))
        assert [0L, 1L, 4L, 9L] == integerMath.toFunction(iMap).apply()
        def iMapNeg = integerMath.map(iMap, integerMath.neg(iLoad(0)))
        assert [0L, -1L, -4L, -9L] == integerMath.toFunction(iMapNeg).apply()
        def iReduce = integerMath.reduce(iMapNeg, iConst(0), integerMath.add(iLoad(0), iLoad(1)))
        assert -14L == integerMath.toFunction(iReduce).apply()

        def fMap = floatMath.map(integerMath.range(iConst(0), iConst(4)), floatMath.mul(l2d.cast(iLoad(0)), fConst(1.0)))
        assert [0.0d, 1.0d, 2.0d, 3.0d, 4.0d] == floatMath.toFunction(fMap).apply()
        def fMapSub = floatMath.map(fMap, floatMath.sub(fLoad(0), fConst(5)))
        assert [-5.0d, -4.0d, -3.0d, -2.0d, -1.0d] == floatMath.toFunction(fMapSub).apply()
        def fReduce = floatMath.reduce(fMapSub, fConst(1), floatMath.mul(fLoad(0), fLoad(1)))
        assert -120.0d == floatMath.toFunction(fReduce).apply()
    }

    @Test
    void "test map/reduce does not call lambda for empty range"() {
        def emptyRange = integerMath.range(iConst(3), iConst(0))
        def nonEmptyRange = integerMath.range(iConst(3), iConst(4))
        def fBadFunc = floatMath.div(l2d.cast(iLoad(0)), fConst(0.0))
        def iBadFunc = integerMath.div(iLoad(0), iConst(0))

        assert [] == floatMath.toFunction(floatMath.map(emptyRange, fBadFunc)).apply()
        assert 1.0d == floatMath.toFunction(floatMath.reduce(emptyRange, fConst(1.0), floatMath.add(fLoad(0), fBadFunc))).apply()

        assert [] == integerMath.toFunction(integerMath.map(emptyRange, iBadFunc)).apply()
        shouldFail ArithmeticException, { integerMath.toFunction(integerMath.map(nonEmptyRange, iBadFunc)).apply() }
        assert 1L == integerMath.toFunction(integerMath.reduce(emptyRange, iConst(1), integerMath.add(iLoad(0), iBadFunc))).apply()
    }
}

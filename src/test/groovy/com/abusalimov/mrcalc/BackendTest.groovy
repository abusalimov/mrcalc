package com.abusalimov.mrcalc

import com.abusalimov.mrcalc.backend.Backend
import com.abusalimov.mrcalc.backend.NumberMath
import com.abusalimov.mrcalc.backend.impl.exprfunc.FuncBackendImpl
import com.abusalimov.mrcalc.runtime.Runtime
import com.abusalimov.mrcalc.runtime.impl.stream.StreamRuntime
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail

/**
 * @author Eldar Abusalimov
 */
class BackendTest<E> {
    private Runtime runtime = new StreamRuntime()
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

    def getSequenceRange(Class<? extends Number> elementType) {
        backend.getSequenceRange(elementType)
    }

    def getSequenceReduce(Class<?> returnType) {
        backend.getSequenceReduce(returnType)
    }

    def getSequenceMap(Class<?> returnElementType, Class<?> elementType) {
        backend.getSequenceMap(returnElementType, elementType)
    }

    def getIntegerMath() {
        (NumberMath<Long, E>) getNumberMath(long)
    }

    def getFloatMath() {
        (NumberMath<Double, E>) getNumberMath(double)
    }

    def getL2d() {
        getNumberCast(double, long)
    }

    def getD2l() {
        getNumberCast(long, double)
    }

    def iLoad(int n) {
        backend.getArgumentLoad(long).load(n)
    }

    def iConst(long l) {
        integerMath.constant(l)
    }

    def fLoad(int n) {
        backend.getArgumentLoad(double).load(n)
    }

    def fConst(double d) {
        floatMath.constant(d)
    }

    @Test
    void "test constant"() {
        def constant = integerMath.constant(42L)
        def fn = integerMath.toEvaluable(constant)
        assert 42 == fn(runtime)
    }

    @Test
    void "test load variable"() {
        def load = iLoad(0)
        def fn = integerMath.toEvaluable(load)
        assert 0 == fn(runtime, 0L)
        assert 7 == fn(runtime, 7L)
    }

    @Test
    void "test expressions with variables"() {
        def iAdd = integerMath.toEvaluable(integerMath.add(iLoad(0), iLoad(1)))
        assert 7L == iAdd(runtime, 3L, 4L)
        assert 150L == iAdd(runtime, 100L, 50L)

        def iMul = integerMath.toEvaluable(integerMath.mul(iLoad(0), iConst(42)))
        assert 0L == iMul(runtime, 0L)
        assert 294L == iMul(runtime, 7L)

        def iPow = integerMath.toEvaluable(integerMath.pow(iLoad(0), iLoad(1)))
        assert 27L == iPow(runtime, 3L, 3L)
        assert 32L == iPow(runtime, 2L, 5L)
    }

    @Ignore
    @Test
    void "test cast"() {
        shouldFail ClassCastException, {
            floatMath.toEvaluable(floatMath.mul(iConst(10), fConst(2))).call(runtime)
        }
        assert 20.0d == integerMath.toEvaluable(floatMath.mul(l2d.cast(iConst(10)), fConst(2))).call(runtime)

        shouldFail ClassCastException, {
            integerMath.toEvaluable(integerMath.mul(iConst(10), fConst(2))).call(runtime)
        }
        assert 20L == integerMath.toEvaluable(integerMath.mul(iConst(10), d2l.cast(fConst(2)))).call(runtime)
    }

    @Test
    void "test ranges"() {
        assert [0L, 1L, 2L, 3L] == integerMath.toEvaluable(getSequenceRange(long).range(iConst(0), iConst(3))).call(runtime)
        assert [] == integerMath.toEvaluable(getSequenceRange(long).range(iConst(3), iConst(0))).call(runtime)
        shouldFail ClassCastException, {
            integerMath.toEvaluable(getSequenceRange(long).range(iConst(0), fConst(2))).call(runtime)
        }
    }

    @Test
    void "test map/reduce"() {
        def iMap = getSequenceMap(long, long).map(getSequenceRange(long).range(iConst(0), iConst(3)), integerMath.pow(iLoad(0), iConst(2)))
        assert [0L, 1L, 4L, 9L] == integerMath.toEvaluable(iMap).call(runtime)
        def iMapNeg = getSequenceMap(long, long).map(iMap, integerMath.neg(iLoad(0)))
        assert [0L, -1L, -4L, -9L] == integerMath.toEvaluable(iMapNeg).call(runtime)
        def iReduce = getSequenceReduce(long).reduce(iMapNeg, iConst(0), integerMath.add(iLoad(0), iLoad(1)))
        assert -14L == integerMath.toEvaluable(iReduce).call(runtime)

        def fMap = getSequenceMap(double, long).map(getSequenceRange(long).range(iConst(0), iConst(4)), floatMath.mul(l2d.cast(iLoad(0)), fConst(1.0)))
        assert [0.0d, 1.0d, 2.0d, 3.0d, 4.0d] == floatMath.toEvaluable(fMap).call(runtime)
        def fMapSub = getSequenceMap(double, double).map(fMap, floatMath.sub(fLoad(0), fConst(5)))
        assert [-5.0d, -4.0d, -3.0d, -2.0d, -1.0d] == floatMath.toEvaluable(fMapSub).call(runtime)
        def fReduce = getSequenceReduce(double).reduce(fMapSub, fConst(1), floatMath.mul(fLoad(0), fLoad(1)))
        assert -120.0d == floatMath.toEvaluable(fReduce).call(runtime)
    }

    @Test
    void "test map/reduce does not call lambda for empty range"() {
        def emptyRange = getSequenceRange(long).range(iConst(3), iConst(0))
        def nonEmptyRange = getSequenceRange(long).range(iConst(3), iConst(4))
        def fBadFunc = floatMath.div(l2d.cast(iLoad(0)), fConst(0.0))
        def iBadFunc = integerMath.div(iLoad(0), iConst(0))

        assert [] == floatMath.toEvaluable(getSequenceMap(double, long).map(emptyRange, fBadFunc)).call(runtime)
        assert 1.0d == floatMath.toEvaluable(getSequenceReduce(double).reduce(emptyRange, fConst(1.0), floatMath.add(fLoad(0), fBadFunc))).call(runtime)

        assert [] == integerMath.toEvaluable(getSequenceMap(long, long).map(emptyRange, iBadFunc)).call(runtime)
        shouldFail ArithmeticException, {
            integerMath.toEvaluable(getSequenceMap(long, long).map(nonEmptyRange, iBadFunc)).call(runtime)
        }
        assert 1L == integerMath.toEvaluable(getSequenceReduce(long).reduce(emptyRange, iConst(1), integerMath.add(iLoad(0), iBadFunc))).call(runtime)
    }
}

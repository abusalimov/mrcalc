package com.abusalimov.mrcalc

import com.abusalimov.mrcalc.backend.Backend
import com.abusalimov.mrcalc.backend.NumberMath
import com.abusalimov.mrcalc.backend.impl.exprfunc.FuncBackendImpl
import com.abusalimov.mrcalc.runtime.Runtime
import com.abusalimov.mrcalc.runtime.Sequence
import com.abusalimov.mrcalc.runtime.impl.stream.StreamRuntime
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail

/**
 * @author Eldar Abusalimov
 */
class BackendTest<E, F> {
    private Runtime runtime = new StreamRuntime()
    private Backend<E, F> backend

    @Before
    void setUp() {
        backend = new FuncBackendImpl<>() as Backend<E, F>
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

    def iConst(long l) {
        integerMath.constant(l)
    }

    def fConst(double d) {
        floatMath.constant(d)
    }

    def createFasm(Class<?> returnType, List<? extends Class<?>> parameterTypes) {
        return backend.createFunctionAssembler(returnType, parameterTypes.toArray(new Class<>[0]))
    }

    def createFasm(Class<?> returnType, Class<?>... parameterTypes) {
        return backend.createFunctionAssembler(returnType, parameterTypes)
    }

    def lambda(Class<?> returnType, Object... parameterTypesAndClosure) {
        def parameterTypes = parameterTypesAndClosure.init().toList() as List<Class>
        def closure = parameterTypesAndClosure.last() as Closure<E>
        def expr = closure()
        def fasm = createFasm(returnType, parameterTypes)
        def func = fasm.assemble(expr)
        fasm.lambda(func)
    }

    @Test
    void "test constant"() {
        def constant = integerMath.constant(42L)
        def fn = createFasm(long).call(constant)
        assert 42 == fn(runtime)
    }

    @Test
    void "test load variable"() {
        def fasm = createFasm(long, long)
        def fn = fasm(fasm.iLoad(0))
        assert 0 == fn(runtime, 0L)
        assert 7 == fn(runtime, 7L)
    }

    @Test
    void "test add of two variables"() {
        def fasm = createFasm(long, long, long)
        def iAdd = fasm(integerMath.add(fasm.iLoad(0), fasm.iLoad(1)))
        assert 7L == iAdd(runtime, 3L, 4L)
        assert 150L == iAdd(runtime, 100L, 50L)
    }

    @Test
    void "test mul of variables and constant"() {
        def fasm = createFasm(long, long)
        def iMul = fasm(integerMath.mul(fasm.iLoad(0), iConst(42)))
        assert 0L == iMul(runtime, 0L)
        assert 294L == iMul(runtime, 7L)
    }

    @Test
    void "test pow of two variables"() {
        def fasm = createFasm(long, long, long)
        def iPow = fasm(integerMath.pow(fasm.iLoad(0), fasm.iLoad(1)))
        assert 27L == iPow(runtime, 3L, 3L)
        assert 32L == iPow(runtime, 2L, 5L)
    }

    @Ignore
    @Test
    void "test cast"() {
        shouldFail ClassCastException, {
            createFasm(long).call(floatMath.mul(iConst(10), fConst(2))).call(runtime)
        }
        assert 20.0d == createFasm(long).call(floatMath.mul(l2d.cast(iConst(10)), fConst(2))).call(runtime)

        shouldFail ClassCastException, {
            createFasm(long).call(integerMath.mul(iConst(10), fConst(2))).call(runtime)
        }
        assert 20L == createFasm(long).call(integerMath.mul(iConst(10), d2l.cast(fConst(2)))).call(runtime)
    }

    @Test
    void "test ranges"() {
        assert [0L, 1L, 2L, 3L] == createFasm(Sequence).call(getSequenceRange(long).range(iConst(0), iConst(3))).call(runtime)
        assert [] == createFasm(Sequence).call(getSequenceRange(long).range(iConst(3), iConst(0))).call(runtime)
//        shouldFail ClassCastException, {
//            createFasm(com.abusalimov.mrcalc.runtime.Sequence).call(getSequenceRange(long).range(iConst(0), fConst(2))).call(runtime)
//        }
    }

    @Test
    void "test map/reduce on long sequences"() {
        def fasm = createFasm(long)

        def iMapLambda = lambda(long, long) { integerMath.pow(fasm.iLoad(0), iConst(2)) }
        def iMap = getSequenceMap(long, long).map(getSequenceRange(long).range(iConst(0), iConst(3)), iMapLambda)
        assert [0L, 1L, 4L, 9L] == fasm(iMap).call(runtime)

        def iMapNegLambda = lambda(long, long) { integerMath.neg(fasm.iLoad(0)) }
        def iMapNeg = getSequenceMap(long, long).map(iMap, iMapNegLambda)
        assert [0L, -1L, -4L, -9L] == fasm(iMapNeg).call(runtime)

        def iReduceLambda = lambda(long, long, long) { integerMath.add(fasm.iLoad(0), fasm.iLoad(1)) }
        def iReduce = getSequenceReduce(long).reduce(iMapNeg, iConst(0), iReduceLambda)
        assert -14L == fasm(iReduce).call(runtime)
    }

    @Test
    void "test map/reduce on double sequences"() {
        def fasm = createFasm(double)

        def fMapLambda = lambda(double, long) { floatMath.mul(l2d.cast(fasm.iLoad(0)), fConst(1.0)) }
        def fMap = getSequenceMap(double, long).map(getSequenceRange(long).range(iConst(0), iConst(4)), fMapLambda)
        assert [0.0d, 1.0d, 2.0d, 3.0d, 4.0d] == fasm(fMap).call(runtime)

        def fMapSubLambda = lambda(double, double) { floatMath.sub(fasm.fLoad(0), fConst(5)) }
        def fMapSub = getSequenceMap(double, double).map(fMap, fMapSubLambda)
        assert [-5.0d, -4.0d, -3.0d, -2.0d, -1.0d] == fasm(fMapSub).call(runtime)

        def fReduceLambda = lambda(double, double, double) { floatMath.mul(fasm.fLoad(0), fasm.fLoad(1)) }
        def fReduce = getSequenceReduce(double).reduce(fMapSub, fConst(1), fReduceLambda)
        assert -120.0d == fasm(fReduce).call(runtime)
    }

    @Test
    void "test map/reduce does not call lambda for empty range"() {
        def fasm = createFasm(double)

        def emptyRange = getSequenceRange(long).range(iConst(3), iConst(0))
        def nonEmptyRange = getSequenceRange(long).range(iConst(3), iConst(4))

        def fBadLambda1 = lambda(double, long) { floatMath.div(l2d.cast(fasm.iLoad(0)), fConst(0.0)) }
        assert [] == createFasm(Sequence).call(getSequenceMap(double, long).map(emptyRange, fBadLambda1)).call(runtime)

        def fBadLambda2 = lambda(double, double, double) {
            floatMath.add(fasm.fLoad(0), floatMath.div(fasm.fLoad(0), fConst(0.0)))
        }
        assert 1.0d == createFasm(double).call(getSequenceReduce(double).reduce(emptyRange, fConst(1.0), fBadLambda2)).call(runtime)

        def iBadLambda = lambda(long, long) { integerMath.div(fasm.iLoad(0), iConst(0)) }
        assert [] == createFasm(Sequence).call(getSequenceMap(long, long).map(emptyRange, iBadLambda)).call(runtime)
        shouldFail ArithmeticException, {
            createFasm(Sequence).call(getSequenceMap(long, long).map(nonEmptyRange, iBadLambda)).call(runtime)
        }
        assert 1L == createFasm(long).call(getSequenceReduce(long).reduce(emptyRange, iConst(1), integerMath.add(fasm.iLoad(0), iBadLambda))).call(runtime)
    }
}

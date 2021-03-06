package com.abusalimov.mrcalc

import com.abusalimov.mrcalc.backend.Backend
import com.abusalimov.mrcalc.backend.FunctionAssembler
import com.abusalimov.mrcalc.backend.impl.bytebuddy.BytebuddyBackendImpl
import com.abusalimov.mrcalc.backend.impl.exprfunc.FuncBackendImpl
import com.abusalimov.mrcalc.runtime.Runtime
import com.abusalimov.mrcalc.runtime.Sequence
import com.abusalimov.mrcalc.runtime.impl.stream.StreamRuntime
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

import static groovy.test.GroovyAssert.shouldFail

/**
 * Unit tests for Backend implementation.
 *
 * @author Eldar Abusalimov
 */
@RunWith(Parameterized.class)
class BackendTest<E, F> {
    private Runtime runtime = new StreamRuntime()
    private Backend<E, F> backend

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        [[new FuncBackendImpl()] as Object[],
         [new BytebuddyBackendImpl<>()] as Object[]]
    }

    BackendTest(Backend<E, F> backend) {
        this.backend = backend
    }

    public <R> FunctionAssembler<R, E, F> createFasm(Class<R> returnType, Class<?>... parameterTypes) {
        backend.createFunctionAssembler(returnType, parameterTypes)
    }

    @Test
    void "test constant"() {
        def fasm = createFasm(long)
        def fn = fasm.call(fasm.lConst(42L))
        assert 42L == fn(runtime)
    }

    @Test
    void "test load variable"() {
        def fasm = createFasm(long, long)
        def fn = fasm(fasm.lLoad(0))
        assert 0L == fn(runtime, 0L)
        assert 7L == fn(runtime, 7L)
    }

    @Test
    void "test add of two long variables"() {
        def fasm = createFasm(long, long, long)
        def iAdd = fasm(fasm.lMath.add(fasm.lLoad(0), fasm.lLoad(1)))
        assert 7L == iAdd(runtime, 3L, 4L)
        assert 150L == iAdd(runtime, 100L, 50L)
    }

    @Test
    void "test add of two double variables"() {
        def fasm = createFasm(double, double, double)
        def dAdd = fasm(fasm.dMath.add(fasm.dLoad(0), fasm.dLoad(1)))
        assert 3.0D.isCloseTo(dAdd(runtime, 1.0D, 2.0D))
        assert (-1.0D).isCloseTo(dAdd(runtime, 1.0D, -2.0D))
    }

    @Test
    void "test add of double and casted long variables"() {
        def fasm = createFasm(double, double, long)
        def add = fasm(fasm.dMath.add(fasm.dLoad(0), fasm.l2d.cast(fasm.lLoad(1))))
        assert 5.0D.isCloseTo(add(runtime, 1.0D, 4L))
        assert (-37.5D).isCloseTo(add(runtime, 12.5D, -50L))
    }

    @Test
    void "test mul of variables and constant"() {
        def fasm = createFasm(long, long)
        def iMul = fasm(fasm.lMath.mul(fasm.lLoad(0), fasm.lConst(42)))
        assert 0L == iMul(runtime, 0L)
        assert 294L == iMul(runtime, 7L)
    }

    @Test
    void "test pow of two long variables"() {
        def fasm = createFasm(long, long, long)
        def iPow = fasm(fasm.lMath.pow(fasm.lLoad(0), fasm.lLoad(1)))
        assert 1L == iPow(runtime, 42L, 0L)
        assert 27L == iPow(runtime, 3L, 3L)
        assert 32L == iPow(runtime, 2L, 5L)
        shouldFail ArithmeticException, { iPow(runtime, 0L, -1L) }
    }

    @Test
    void "test pow of two double variables"() {
        def fasm = createFasm(double, double, double)
        def dPow = fasm(fasm.dMath.pow(fasm.dLoad(0), fasm.dLoad(1)))
        assert 1D == dPow(runtime, 42D, 0D)
        assert 27D == dPow(runtime, 3D, 3D)
        assert 32D == dPow(runtime, 2D, 5D)
        assert Double.NaN == dPow(runtime, -1D, 0.5D)
        assert Double.POSITIVE_INFINITY == dPow(runtime, 0D, -1D)
    }

    @Test
    void "test ranges"() {
        def fasm = createFasm(Sequence)
        assert [0L, 1L, 2L, 3L] == fasm.call(fasm.getSequenceRange(long).range(fasm.lConst(0), fasm.lConst(3))).eval(runtime)
        assert [] == fasm.call(fasm.getSequenceRange(long).range(fasm.lConst(3), fasm.lConst(0))).eval(runtime)
    }

    @Test
    void "test reduce on long sequences"() {
        def fasm = createFasm(long)

        def iReduceLambda = fasm.lambda(createFasm(long, long, long)) { fasm.lMath.add(fasm.lLoad(0), fasm.lLoad(1)) }
        def iReduce = fasm.getSequenceReduce(long).reduce(fasm.getSequenceRange(long).range(fasm.lConst(-5), fasm.lConst(1)), fasm.lConst(0), iReduceLambda)
        assert -14L == fasm(iReduce).eval(runtime)
    }

    @Test
    void "test reduce on double sequences"() {
        def fasm = createFasm(double, Sequence.OfDouble)

        def sequenceOfDouble = runtime.mapLongToDouble(runtime.createLongRangeInclusive(-5L, -1L), { (double) it })
        assert [-5.0d, -4.0d, -3.0d, -2.0d, -1.0d] == sequenceOfDouble

        def fReduceLambda = fasm.lambda(createFasm(double, double, double)) {
            fasm.dMath.mul(fasm.dLoad(0), fasm.dLoad(1))
        }
        def fReduce = fasm.getSequenceReduce(double).reduce(fasm.getArgumentLoad(Sequence.OfDouble).load(0), fasm.dConst(1), fReduceLambda)
        assert -120.0d == fasm(fReduce).eval(runtime, sequenceOfDouble)
    }

    @Test
    void "test map on long sequences"() {
        def fasm = createFasm(Sequence.OfLong)

        def iMapLambda = fasm.lambda(createFasm(long, long)) { fasm.lMath.pow(fasm.lLoad(0), fasm.lConst(2)) }
        def iMap = fasm.getSequenceMap(long, long).map(fasm.getSequenceRange(long).range(fasm.lConst(0), fasm.lConst(3)), iMapLambda)
        assert [0L, 1L, 4L, 9L] == fasm(iMap).eval(runtime)

        def iMapNegLambda = fasm.lambda(createFasm(long, long)) { fasm.lMath.neg(fasm.lLoad(0)) }
        def iMapNeg = fasm.getSequenceMap(long, long).map(iMap, iMapNegLambda)
        assert [0L, -1L, -4L, -9L] == fasm(iMapNeg).eval(runtime)
    }

    @Test
    void "test map on double sequences"() {
        def fasm = createFasm(Sequence.OfDouble)

        def fMapLambda = fasm.lambda(createFasm(double, long)) {
            fasm.dMath.mul(fasm.l2d.cast(fasm.lLoad(0)), fasm.dConst(1.0))
        }
        def fMap = fasm.getSequenceMap(double, long).map(fasm.getSequenceRange(long).range(fasm.lConst(0), fasm.lConst(4)), fMapLambda)
        assert [0.0d, 1.0d, 2.0d, 3.0d, 4.0d] == fasm(fMap).eval(runtime)

        def fMapSubLambda = fasm.lambda(createFasm(double, double)) { fasm.dMath.sub(fasm.dLoad(0), fasm.dConst(5)) }
        def fMapSub = fasm.getSequenceMap(double, double).map(fMap, fMapSubLambda)
        assert [-5.0d, -4.0d, -3.0d, -2.0d, -1.0d] == fasm(fMapSub).eval(runtime)
    }

    @Test
    void "test map/reduce does not call lambda for empty range"() {
        def fasm = createFasm(Sequence.OfLong)

        def emptyRange = fasm.getSequenceRange(long).range(fasm.lConst(3), fasm.lConst(0))
        def nonEmptyRange = fasm.getSequenceRange(long).range(fasm.lConst(3), fasm.lConst(4))

        def iBadLambda = fasm.lambda(createFasm(long, long)) { fasm.lMath.div(fasm.lLoad(0), fasm.lConst(0)) }
        assert [] == fasm.call(fasm.getSequenceMap(long, long).map(emptyRange, iBadLambda)).eval(runtime)
        shouldFail ArithmeticException, {
            fasm.call(fasm.getSequenceMap(long, long).map(nonEmptyRange, iBadLambda)).eval(runtime)
        }

//        def lFasm = createFasm(long)
//        assert 1L == lFasm.call(fasm.getSequenceReduce(long).reduce(emptyRange, fasm.lConst(1), fasm.lMath.add(fasm.lLoad(0), iBadLambda))).eval(runtime)
    }
}

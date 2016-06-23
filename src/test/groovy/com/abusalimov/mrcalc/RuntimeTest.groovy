package com.abusalimov.mrcalc

import com.abusalimov.mrcalc.runtime.Runtime
import com.abusalimov.mrcalc.runtime.StreamRuntime
import org.junit.Before
import org.junit.Test

import java.util.function.BinaryOperator
import java.util.function.LongBinaryOperator

/**
 * @author Eldar Abusalimov
 */
class RuntimeTest {
    private Runtime runtime

    @Before
    void setUp() {
        runtime = new StreamRuntime()
    }

    @Test
    void "test createLongRange"() {
        assert [] as List<Long> == runtime.createLongRange(0, 0)
        assert [] as List<Long> == runtime.createLongRange(1, -1)
        assert [0L] as List<Long> == runtime.createLongRange(0, 1)
        assert [0L, 1L, 2L, 3L] as List<Long> == runtime.createLongRange(0, 4)
        assert [-2L, -1L, 0L, 1L, 2L] as List<Long> == runtime.createLongRange(-2, 3)

    }

    @Test
    void "test map for longs"() {
        def seq = runtime.createLongRange(0, 4)
        assert [0L, 1L, 2L, 3L] as List<Long> == seq

        assert [0L, 0L, 0L, 0L] as List<Long> == runtime.mapToObject(seq, { 0L })
        assert [0L, 0L, 0L, 0L] as List<Long> == runtime.mapToLong(seq, { 0L })
        assert [0L, 0L, 0L, 0L] as List<Long> == runtime.mapLongToObject(seq, { 0L })
        assert [0L, 0L, 0L, 0L] as List<Long> == runtime.mapLongToLong(seq, { 0L })

        assert [0L, -1L, -2L, -3L] as List<Long> == runtime.mapToObject(seq, { -it })
        assert [0L, -1L, -2L, -3L] as List<Long> == runtime.mapToLong(seq, { -it })
        assert [0L, -1L, -2L, -3L] as List<Long> == runtime.mapLongToObject(seq, { -it })
        assert [0L, -1L, -2L, -3L] as List<Long> == runtime.mapLongToLong(seq, { -it })
    }

    @Test
    void "test map for objects"() {
        def seq = runtime.createLongRange(0, 4)
        assert [0L, 1L, 2L, 3L] as List<Long> == seq

        assert [null, null, null, null] == runtime.mapToObject(seq, { null })
        assert [null, null, null, null] == runtime.mapLongToObject(seq, { null })

        assert [[0L], [-1L], [-2L], [-3L]] == runtime.mapToObject(seq, { [-it] })
        assert [[0L], [-1L], [-2L], [-3L]] == runtime.mapLongToObject(seq, { [-it] })
    }

    @Test
    void "test reduce"() {
        def seq = runtime.createLongRange(0, 4)
        assert [0L, 1L, 2L, 3L] as List<Long> == seq

        assert 6L == runtime.reduceLong(seq, 0L, { a, b -> a + b } as LongBinaryOperator)
        assert 6L == runtime.reduce(seq, 0L, { a, b -> a + b } as BinaryOperator<Long>)

        def objSeq = runtime.mapLongToObject(seq, { [0L] })
        assert [[0L], [0L], [0L], [0L]] == objSeq

        assert [] == runtime.reduce(objSeq, [], { a, b -> [] } as BinaryOperator)
    }

    @Test
    void "test reduce for objects"() {
        def objSeq = runtime.mapLongToObject(runtime.createLongRange(0, 4), { [0L] })
        assert [[0L], [0L], [0L], [0L]] == objSeq

        assert [] == runtime.reduce(objSeq, [], { a, b -> [] } as BinaryOperator)
    }
}

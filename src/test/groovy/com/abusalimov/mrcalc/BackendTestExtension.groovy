package com.abusalimov.mrcalc

import com.abusalimov.mrcalc.backend.FunctionAssembler
import com.abusalimov.mrcalc.backend.NumberCast
import com.abusalimov.mrcalc.backend.NumberMath
import com.abusalimov.mrcalc.runtime.Evaluable
import com.abusalimov.mrcalc.runtime.Runtime

/**
 * Handy extension methods for better tests readability.
 *
 * @author Eldar Abusalimov
 */
class BackendTestExtension {
    static <R> R call(final Evaluable<R> self, Runtime runtime, Object... args) {
        self.eval(runtime, args)
    }

    static <R, E, F> Evaluable<R> call(final FunctionAssembler<R, E, F> self, E expr) {
        self.toEvaluable(self.assemble(expr));
    }

    static <R, E, F> E lambda(final FunctionAssembler<R, E, F> self, FunctionAssembler<?, E, F> fasm,
                              Closure<E> closure) {
        def expr = closure()
        def func = fasm.assemble(expr)
        self.lambda(func)
    }

    static <E> E lLoad(final FunctionAssembler<?, E, ?> self, int slot) {
        self.getArgumentLoad(long).load(slot)
    }

    static <E> E dLoad(final FunctionAssembler<?, E, ?> self, int slot) {
        self.getArgumentLoad(double).load(slot)
    }

    static <R, E, F> NumberMath<Long, E> getlMath(final FunctionAssembler<R, E, F> self) {
        self.getNumberMath(Long.TYPE)
    }

    static <R, E, F> NumberMath<Double, E> getdMath(final FunctionAssembler<R, E, F> self) {
        self.getNumberMath(Double.TYPE)
    }

    static <R, E, F> E lConst(final FunctionAssembler<R, E, F> self, long l) {
        self.lMath.constant(l)
    }

    static <R, E, F> E dConst(final FunctionAssembler<R, E, F> self, double d) {
        self.dMath.constant(d)
    }

    static <R, E, F> NumberCast<E, E> getL2d(final FunctionAssembler<R, E, F> self) {
        self.getNumberCast(double, long)
    }

    static <R, E, F> NumberCast<E, E> getD2l(final FunctionAssembler<R, E, F> self) {
        self.getNumberCast(long, double)
    }

    static <E, F> E call(final NumberCast<E, F> self, F from) {
        self.cast(from)
    }

    static boolean isCloseTo(Double self, Object other) {
        other instanceof Double && (Math.abs(self - other) < 1e-10)
    }
}

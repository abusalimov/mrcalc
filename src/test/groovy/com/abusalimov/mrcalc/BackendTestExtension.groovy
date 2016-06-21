package com.abusalimov.mrcalc

import com.abusalimov.mrcalc.backend.FunctionAssembler
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

    static <E> E iLoad(final FunctionAssembler<?, E, ?> self, int slot) {
        self.getArgumentLoad(long).load(slot)
    }

    static <E> E fLoad(final FunctionAssembler<?, E, ?> self, int slot) {
        self.getArgumentLoad(double).load(slot)
    }

}

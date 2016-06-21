package com.abusalimov.mrcalc

import com.abusalimov.mrcalc.runtime.Evaluable
import com.abusalimov.mrcalc.runtime.Runtime

/**
 * Handy extension methods for better tests readability.
 *
 * @author Eldar Abusalimov
 */
class BackendTestExtension {
    static <T> T call(final Evaluable<T> self, Runtime runtime, Object... args) {
        self.eval(runtime, args)
    }
}

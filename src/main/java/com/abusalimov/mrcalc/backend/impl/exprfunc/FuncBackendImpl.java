package com.abusalimov.mrcalc.backend.impl.exprfunc;

import com.abusalimov.mrcalc.backend.Backend;
import com.abusalimov.mrcalc.backend.FunctionAssembler;

/**
 * The backend implementation using functions as the expression type and evaluating expressions by calling the
 * corresponding functions.
 *
 * @author Eldar Abusalimov
 */
public class FuncBackendImpl implements Backend<Func<?>, Func<?>> {
    @SuppressWarnings("unchecked")
    @Override
    public <R> FunctionAssembler<R, Func<?>, Func<?>> createFunctionAssembler(Class<R> returnType,
                                                                              Class<?>... parameterTypes) {
        return new FuncAssembler();
    }
}

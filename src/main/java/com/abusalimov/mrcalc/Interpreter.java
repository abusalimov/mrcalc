package com.abusalimov.mrcalc;

import com.abusalimov.mrcalc.ast.NodeVisitor;
import com.abusalimov.mrcalc.compile.Code;

import java.util.function.ToLongFunction;

/**
 * @author Eldar Abusalimov
 */
public class Interpreter implements NodeVisitor<Long> {
    public Long eval(Code code) {
        return ((ToLongFunction<Object[]>) code.getExpr()).applyAsLong(null);
    }
}

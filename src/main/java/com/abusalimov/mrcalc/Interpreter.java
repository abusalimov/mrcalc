package com.abusalimov.mrcalc;

import com.abusalimov.mrcalc.ast.NodeVisitor;
import com.abusalimov.mrcalc.compile.Code;

import java.util.function.LongSupplier;

/**
 * @author Eldar Abusalimov
 */
public class Interpreter implements NodeVisitor<Long> {
    public Long eval(Code code) {
        return ((LongSupplier) code.getExpr()).getAsLong();  // XXX
    }
}

package com.abusalimov.mrcalc;

import com.abusalimov.mrcalc.compile.Code;

/**
 * @author Eldar Abusalimov
 */
public class Interpreter {
    public Object eval(Code code) {
        return code.getExprFunction().apply(null);
    }
}

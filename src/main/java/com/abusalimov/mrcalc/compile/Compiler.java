package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.ast.Node;
import com.abusalimov.mrcalc.ast.expr.ExprNode;
import com.abusalimov.mrcalc.diagnostic.AbstractDiagnosticEmitter;

/**
 * @author Eldar Abusalimov
 */
public class Compiler extends AbstractDiagnosticEmitter {
    public Code compile(Node node) throws CompileErrorException {
        return new Code((ExprNode) node);  // FIXME cast
    }
}

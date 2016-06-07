package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.ast.Node;
import com.abusalimov.mrcalc.diagnostic.AbstractDiagnosticEmitter;
import com.abusalimov.mrcalc.diagnostic.Diagnostic;

/**
 * @author Eldar Abusalimov
 */
public abstract class AbstractNodeDiagnosticEmitter extends AbstractDiagnosticEmitter {
    protected void emitNodeDiagnostic(Node node, String message) {
        emitDiagnostic(new Diagnostic(node.getLocation(), message));
    }
}

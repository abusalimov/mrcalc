package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.ast.Node;
import com.abusalimov.mrcalc.diagnostic.AbstractDiagnosticEmitter;
import com.abusalimov.mrcalc.diagnostic.Diagnostic;

/**
 * Provides a helper method for reporting a {@link Diagnostic} with a location of an AST node.
 *
 * @author Eldar Abusalimov
 */
public abstract class AbstractNodeDiagnosticEmitter extends AbstractDiagnosticEmitter {
    /**
     * Creates and emits a new {@link Diagnostic} with its location taken from the specified node
     *
     * @param node    the AST node to take the location from
     * @param message the diagnostic message
     */
    protected void emitNodeDiagnostic(Node node, String message) {
        emitDiagnostic(new Diagnostic(node.getLocation(), message));
    }
}

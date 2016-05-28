package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.ast.Node;
import com.abusalimov.mrcalc.ast.expr.ExprNode;
import com.abusalimov.mrcalc.diagnostic.DiagnosticListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eldar Abusalimov
 */
public class Compiler {
    private final List<DiagnosticListener> diagnosticListeners = new ArrayList<>();

    public Code compile(Node node) throws CompileErrorException {
        return new Code((ExprNode) node);  // FIXME cast
    }

    public void addDiagnosticListener(DiagnosticListener diagnosticListener) {
        diagnosticListeners.add(diagnosticListener);
    }

    public void removeDiagnosticListener(DiagnosticListener diagnosticListener) {
        diagnosticListeners.remove(diagnosticListener);
    }
}

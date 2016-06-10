package com.abusalimov.mrcalc.ui;

import com.abusalimov.mrcalc.Interpreter;
import com.abusalimov.mrcalc.ast.ProgramNode;
import com.abusalimov.mrcalc.compile.CompileErrorException;
import com.abusalimov.mrcalc.compile.Compiler;
import com.abusalimov.mrcalc.compile.Stmt;
import com.abusalimov.mrcalc.diagnostic.Diagnostic;
import com.abusalimov.mrcalc.parse.Parser;
import com.abusalimov.mrcalc.parse.SyntaxErrorException;
import com.abusalimov.mrcalc.parse.impl.antlr.ANTLRParserImpl;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author - Eldar Abusalimov
 */
public class CodeTextPane extends JTextPane {
    private Consumer<List<Diagnostic>> errorListener;
    private List<Diagnostic> diagnostics = Collections.emptyList();

    public CodeTextPane() {
        getStyledDocument().addDocumentListener(new HighlightListener());
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        int offset = viewToModel(event.getPoint());
        for (Diagnostic diagnostic : diagnostics) {
            int startOffset = diagnostic.getLocation().getStartOffset();
            int endOffset = diagnostic.getLocation().getEndOffset();

            try {
                if ((startOffset == endOffset || !onSameLine(startOffset, endOffset)) &&
                    offset == startOffset &&
                    modelToView(offset).x + 8 > event.getPoint().x)
                    return diagnostic.getMessage();

                if (offset >= startOffset && offset < endOffset)
                    return diagnostic.getMessage();

            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
        return super.getToolTipText(event);
    }

    public void setErrorListener(Consumer<List<Diagnostic>> errorListener) {
        this.errorListener = errorListener;
    }

    private void fireErrorListener(List<Diagnostic> diagnostics) {
        if (errorListener != null)
            errorListener.accept(diagnostics);
    }

    private boolean onSameLine(int startOffset, int endOffset) {
        try {
            return getDocument().getText(startOffset, endOffset - startOffset).indexOf('\n') == -1;
        } catch (BadLocationException e) {
            e.printStackTrace();
            return false;
        }
    }

    private class HighlightListener implements DocumentListener {
        private final SquigglePainter squigglePainter = new SquigglePainter(Color.RED);
        private final DefaultHighlighter.DefaultHighlightPainter defaultPainter =
                new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 0, 0, 16));
        private final EOFPainter eofPainter = new EOFPainter(Color.RED);

        @Override
        public void insertUpdate(DocumentEvent e) {
            SwingUtilities.invokeLater(this::handleEvent);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            SwingUtilities.invokeLater(this::handleEvent);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            /* do nothing */
        }

        private void handleEvent() {
            clearHighlight();
            Parser parser = new ANTLRParserImpl();
            Compiler compiler = new Compiler();
            Interpreter interpreter = new Interpreter(System.out);
            try {
                ProgramNode node = parser
                        .parse(getDocument().getText(0, getDocument().getLength()));
                List<Stmt> stmts = compiler.compile(node);
                interpreter.exec(stmts);
            } catch (SyntaxErrorException | CompileErrorException e) {
                diagnostics = e.getDiagnostics();

                fireErrorListener(diagnostics);

                for (Diagnostic diagnostic : diagnostics) {
                    try {
                        highlight(diagnostic);
                    } catch (BadLocationException e1) {
                        e.printStackTrace();
                    }
                }
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }

        private void clearHighlight() {
            diagnostics = Collections.emptyList();
            getHighlighter().removeAllHighlights();
            fireErrorListener(diagnostics);
        }

        private void highlight(Diagnostic diagnostic) throws BadLocationException {
            int startOffset = diagnostic.getLocation().getStartOffset();
            int endOffset = diagnostic.getLocation().getEndOffset();

            if (startOffset == endOffset) {
                getHighlighter().addHighlight(startOffset, endOffset + 1, eofPainter);
            } else if (!onSameLine(startOffset, endOffset)) {
                getHighlighter().addHighlight(startOffset, endOffset, eofPainter);
            } else {
                getHighlighter().addHighlight(startOffset, endOffset, defaultPainter);
                getHighlighter().addHighlight(startOffset, endOffset, squigglePainter);
            }
        }
    }
}

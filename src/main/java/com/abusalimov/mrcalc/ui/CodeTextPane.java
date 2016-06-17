package com.abusalimov.mrcalc.ui;

import com.abusalimov.mrcalc.CalcExecutor;
import com.abusalimov.mrcalc.diagnostic.Diagnostic;

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
    private static int squiggleSize = 2;
    private static int squigglesAtEof = 2;

    private final JTextArea outputTextArea;
    private Consumer<List<Diagnostic>> errorListener;
    private List<Diagnostic> diagnostics = Collections.emptyList();

    public CodeTextPane(CalcExecutor calcExecutor, JTextArea outputTextArea) {
        this.outputTextArea = outputTextArea;
        getStyledDocument().addDocumentListener(new HighlightListener(calcExecutor));
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
                    modelToView(offset).x + squigglesAtEof * squiggleSize > event.getPoint().x)
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
        private final SquigglePainter squigglePainter = new SquigglePainter(Color.RED, squiggleSize);
        private final DefaultHighlighter.DefaultHighlightPainter defaultPainter =
                new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 0, 0, 16));
        private final SquigglePainter eofPainter = new SquigglePainter(Color.RED, squiggleSize) {
            @Override
            protected void paintSquiggles(Graphics g, Rectangle r) {
                super.paintSquiggles(g, new Rectangle(r.x + r.width, r.y, squigglesAtEof *squiggle, r.height));
            }
        };

        private final CalcExecutor calcExecutor;

        public HighlightListener(CalcExecutor calcExecutor) {
            this.calcExecutor = calcExecutor;
            calcExecutor.setCallback(diagnostics -> SwingUtilities.invokeLater(() -> highlight(diagnostics)));
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            handleEvent();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            handleEvent();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            /* do nothing */
        }

        private void handleEvent() {
            clearHighlight();
            outputTextArea.setText("");
            try {
                String sourceCodeText = getDocument().getText(0, getDocument().getLength());
                calcExecutor.execute(sourceCodeText, new TextAreaStream(outputTextArea));
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }

        private void clearHighlight() {
            diagnostics = Collections.emptyList();
            getHighlighter().removeAllHighlights();
            fireErrorListener(diagnostics);
        }

        private void highlight(List<Diagnostic> diagnostics) {
            if (!EventQueue.isDispatchThread())
                throw new IllegalThreadStateException();

            fireErrorListener(diagnostics);
            diagnostics.forEach(this::highlight);
            repaint();
        }

        private void highlight(Diagnostic diagnostic) {
            int startOffset = diagnostic.getLocation().getStartOffset();
            int endOffset = diagnostic.getLocation().getEndOffset();

            try {
                if (startOffset == endOffset) {
                    getHighlighter().addHighlight(startOffset, endOffset + 1, eofPainter);
                } else if (!onSameLine(startOffset, endOffset)) {
                    getHighlighter().addHighlight(startOffset, endOffset, eofPainter);
                } else {
                    getHighlighter().addHighlight(startOffset, endOffset, defaultPainter);
                    getHighlighter().addHighlight(startOffset, endOffset, squigglePainter);
                }
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }
}

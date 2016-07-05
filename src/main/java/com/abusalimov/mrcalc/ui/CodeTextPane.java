package com.abusalimov.mrcalc.ui;

import com.abusalimov.mrcalc.CalcExecutor;
import com.abusalimov.mrcalc.diagnostic.Diagnostic;
import com.abusalimov.mrcalc.diagnostic.DiagnosticCollector;
import com.abusalimov.mrcalc.diagnostic.DiagnosticException;
import com.abusalimov.mrcalc.diagnostic.DiagnosticListener;
import com.abusalimov.mrcalc.location.Location;
import com.abusalimov.mrcalc.runtime.RuntimeErrorException;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rsyntaxtextarea.parser.*;
import org.fife.ui.rtextarea.RTextAreaUI;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * @author - Eldar Abusalimov
 */
public class CodeTextPane extends RSyntaxTextArea {
    private final CalcExecutor calcExecutor;
    private final OutputTextArea outputTextArea;
    private Consumer<List<Diagnostic>> errorListener;
    private RuntimeParser runtimeParser = new RuntimeParser();

    public CodeTextPane(CalcExecutor calcExecutor, OutputTextArea outputTextArea) {
        this.calcExecutor = calcExecutor;
        this.outputTextArea = outputTextArea;
        SwingUtilities.invokeLater(this::requestFocus);
        addParser(new CompileParser());
        addParser(runtimeParser);
        setParserDelay(250);
    }

    @Override
    protected RTextAreaUI createRTextAreaUI() {
        return new RSyntaxTextAreaUI(this) {
            @Override
            protected Highlighter createHighlighter() {
                return new EOFAwareHighlighter();
            }
        };
    }

    public void setErrorListener(Consumer<List<Diagnostic>> errorListener) {
        this.errorListener = errorListener;
    }

    private void fireErrorListener(List<Diagnostic> diagnostics) {
        if (errorListener != null)
            errorListener.accept(new ArrayList<>(diagnostics));
    }

    protected static class EOFAwareHighlighter extends RSyntaxTextAreaHighlighter {
        private final HighlightPainter highlightPainter = new SquiggleUnderlineHighlightPainter(Color.RED) {
            private static final int AMT = 2;

            @Override
            protected void paintSquiggle(Graphics g, Rectangle r) {
                /*
                 * Fixup zero-width highlights to make at least on wave: ^v
                 */
                if (r.width <= AMT) {
                    r = new Rectangle(r);
                    r.width = AMT * 3;
                }
                super.paintSquiggle(g, r);
            }
        };

        @Override
        protected void paintListLayered(Graphics g, int lineStart, int lineEnd, Shape viewBounds,
                                        JTextComponent editor, View view,
                                        List<? extends HighlightInfo> highlights) {
            for (int i = highlights.size() - 1; i >= 0; i--) {
                HighlightInfo tag = highlights.get(i);
                if (tag instanceof HighlightInfoImpl) {
                    HighlightInfoImpl hii = (HighlightInfoImpl) tag;
                    if (hii.getPainter() instanceof SquiggleUnderlineHighlightPainter) {
                        hii.setPainter(highlightPainter);
                    }
                }

                if (tag instanceof LayeredHighlightInfo) {
                    LayeredHighlightInfo lhi = (LayeredHighlightInfo) tag;
                    int highlightStart = lhi.getStartOffset();
                    int highlightEnd = lhi.getEndOffset() + 1;
                    /*
                     * Allow highlight to span one char past EOL.
                     */
                    if ((lineStart < highlightStart && highlightStart <= lineEnd) ||  // <- here is what we patch
                        (highlightStart <= lineStart && lineStart < highlightEnd)) {
                        lhi.paintLayeredHighlights(g, lineStart, lineEnd, viewBounds, editor, view);
                    }
                }
            }
        }
    }

    protected abstract class AbstractDiagnosticParser extends AbstractParser {
        @Override
        public ParseResult parse(RSyntaxDocument doc, String style) {
            DefaultParseResult result = new DefaultParseResult(this);
            try {
                exec();
            } catch (DiagnosticException e) {
                handleDiagnostics(result, e.getDiagnostics());
            }
            return result;
        }

        protected abstract void exec() throws DiagnosticException;

        protected void handleDiagnostics(DefaultParseResult result, List<Diagnostic> diagnostics) {
            for (Diagnostic diagnostic : diagnostics) {
                addDiagnosticToResult(result, diagnostic);
            }
            fireErrorListener(diagnostics);
        }

        protected void addDiagnosticToResult(DefaultParseResult parseResult, Diagnostic diagnostic) {
            Location location = diagnostic.getLocation();
            DefaultParserNotice notice = new DefaultParserNotice(AbstractDiagnosticParser.this,
                    diagnostic.getMessage(),
                    location.getLineNumber(),
                    location.getStartOffset(),
                    Math.max(1, location.getEndOffset() - location.getStartOffset()));
            notice.setLevel(ParserNotice.Level.ERROR);
            parseResult.addNotice(notice);
        }
    }

    private class CompileParser extends AbstractDiagnosticParser {
        @Override
        protected void exec() throws DiagnosticException {
            fireErrorListener(Collections.emptyList());
            outputTextArea.setText("");

            String sourceCodeText;
            try {
                sourceCodeText = getDocument().getText(0, getDocument().getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
                return;
            }

            DiagnosticListener runtimeDiagnosticCollector = runtimeParser.newDiagnosticCollector();

            calcExecutor.execute(sourceCodeText, outputTextArea::createStream, diagnostic ->
                    SwingUtilities.invokeLater(() -> {
                        runtimeDiagnosticCollector.report(diagnostic);

                        /* Hook the parser only once. */
                        runtimeParser.setEnabled(true);
                        forceReparsing(runtimeParser);
                        runtimeParser.setEnabled(false);
                    }));

        }
    }

    private class RuntimeParser extends AbstractDiagnosticParser {
        private DiagnosticCollector diagnosticCollector;

        public DiagnosticListener newDiagnosticCollector() {
            /*
             * It's OK to use CoW list since we usually have at most one runtime diagnostic.
             */
            return diagnosticCollector = new DiagnosticCollector(new CopyOnWriteArrayList<>());
        }

        @Override
        public boolean isEnabled() {
            return diagnosticCollector != null && !diagnosticCollector.getDiagnostics().isEmpty() && super.isEnabled();
        }

        @Override
        protected void exec() throws DiagnosticException {
            if (diagnosticCollector != null && !diagnosticCollector.getDiagnostics().isEmpty()) {
                throw new RuntimeErrorException(diagnosticCollector.getDiagnostics());
            }
            diagnosticCollector = null;
        }

    }
}

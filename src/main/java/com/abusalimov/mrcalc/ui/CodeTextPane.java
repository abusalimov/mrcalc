package com.abusalimov.mrcalc.ui;

import com.abusalimov.mrcalc.CalcExecutor;
import com.abusalimov.mrcalc.diagnostic.Diagnostic;
import com.abusalimov.mrcalc.diagnostic.DiagnosticException;
import com.abusalimov.mrcalc.location.Location;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.parser.*;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author - Eldar Abusalimov
 */
public class CodeTextPane extends RSyntaxTextArea {
    private final CalcExecutor calcExecutor;
    private final OutputTextArea outputTextArea;
    private Consumer<List<Diagnostic>> errorListener;

    public CodeTextPane(CalcExecutor calcExecutor, OutputTextArea outputTextArea) {
        this.calcExecutor = calcExecutor;
        this.outputTextArea = outputTextArea;
        SwingUtilities.invokeLater(this::requestFocus);
        addParser(new CompileParser());
        setParserDelay(250);
    }

    public void setErrorListener(Consumer<List<Diagnostic>> errorListener) {
        this.errorListener = errorListener;
    }

    private void fireErrorListener(List<Diagnostic> diagnostics) {
        if (errorListener != null)
            errorListener.accept(new ArrayList<>(diagnostics));
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

            calcExecutor.execute(sourceCodeText, outputTextArea::createStream);

        }
    }
}

package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.SyntaxErrorException;
import com.abusalimov.mrcalc.ast.Node;
import com.abusalimov.mrcalc.ast.expr.ExprNode;
import com.abusalimov.mrcalc.diagnostic.Diagnostic;
import com.abusalimov.mrcalc.diagnostic.DiagnosticCollector;
import com.abusalimov.mrcalc.diagnostic.DiagnosticListener;
import com.abusalimov.mrcalc.grammar.CalcLexer;
import com.abusalimov.mrcalc.grammar.CalcParser;
import com.abusalimov.mrcalc.location.Location;
import com.abusalimov.mrcalc.location.RawLocation;
import org.antlr.v4.runtime.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Eldar Abusalimov
 */
public class Compiler {
    private final List<DiagnosticListener> diagnosticListeners = new ArrayList<>();

    public Code compile(String s) throws SyntaxErrorException {
        try {
            return compile(new StringReader(Objects.requireNonNull(s)));
        } catch (IOException e) {
            /* StringReader never throws for non-null strings, relax. */
            throw new RuntimeException(e);
        }
    }

    public Code compile(Reader reader) throws IOException, SyntaxErrorException {
        CalcParser.ProgramContext programTree = parse(reader);
        return compile(programTree);
    }

    public Code compile(CalcParser.ProgramContext programTree) throws SyntaxErrorException {
        Node program = new ASTConstructor().visit(programTree);
        return new Code((ExprNode) program);  // FIXME cast
    }

    public CalcParser.ProgramContext parse(
            Reader reader) throws IOException, SyntaxErrorException {
        Lexer lexer = createLexer(reader);
        CalcParser parser = createParser(lexer);

        CalcParser.ProgramContext programContext;

        DiagnosticCollector diagnosticCollector = new DiagnosticCollector();
        addDiagnosticListener(diagnosticCollector);
        try {
            programContext = parser.program();
        } catch (RecognitionException e) {
            /* Should not happen, unless someone overrides the default error recovery strategy */
            throw new SyntaxErrorException(e);
        } finally {
            removeDiagnosticListener(diagnosticCollector);
        }
        List<Diagnostic> collectedDiagnostics = diagnosticCollector.getDiagnostics();
        if (collectedDiagnostics.size() > 0) {
            throw new SyntaxErrorException(collectedDiagnostics);
        }

        return programContext;
    }

    protected <T extends Recognizer> T initRecognizer(T recognizer) {
        recognizer.removeErrorListeners();
        recognizer.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                    int line, int charPositionInLine, String msg,
                                    RecognitionException e) {
                if (diagnosticListeners.isEmpty()) {
                    return;
                }

                Location location;
                if (offendingSymbol instanceof Token) {
                    Token token = (Token) offendingSymbol;
                    location = new TokenLocation(token);
                } else {
                    IntStream stream = e.getInputStream();
                    int offset = stream.index();
                    int endOffset = Math.min(offset + 1, stream.size());
                    location = new RawLocation(line, charPositionInLine, offset, offset,
                            endOffset);
                }
                Diagnostic diagnostic = new Diagnostic(location, msg);

                for (DiagnosticListener diagnosticListener : diagnosticListeners) {
                    diagnosticListener.report(diagnostic);
                }

            }
        });
        return recognizer;
    }

    protected CalcParser createParser(Lexer lexer) {
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        return createParser(tokenStream);
    }

    protected CalcParser createParser(CommonTokenStream tokenStream) {
        return initRecognizer(new CalcParser(tokenStream));
    }

    protected Lexer createLexer(Reader reader) throws IOException {
        ANTLRInputStream input = new ANTLRInputStream(reader);
        return createLexer(input);
    }

    protected Lexer createLexer(CharStream input) {
        return initRecognizer(new CalcLexer(input));
    }

    public void addDiagnosticListener(DiagnosticListener diagnosticListener) {
        diagnosticListeners.add(diagnosticListener);
    }

    public void removeDiagnosticListener(DiagnosticListener diagnosticListener) {
        diagnosticListeners.remove(diagnosticListener);
    }
}

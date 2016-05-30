package com.abusalimov.mrcalc.parse.impl.antlr;

import com.abusalimov.mrcalc.ast.Node;
import com.abusalimov.mrcalc.diagnostic.AbstractDiagnosticEmitter;
import com.abusalimov.mrcalc.diagnostic.Diagnostic;
import com.abusalimov.mrcalc.location.Location;
import com.abusalimov.mrcalc.location.RawLocation;
import com.abusalimov.mrcalc.parse.Parser;
import com.abusalimov.mrcalc.parse.SyntaxErrorException;
import org.antlr.v4.runtime.*;

import java.io.IOException;
import java.io.Reader;

/**
 * @author Eldar Abusalimov
 */
public class ANTLRParserImpl extends AbstractDiagnosticEmitter implements Parser {

    private ASTConstructor astConstructor;

    public ANTLRParserImpl() {
        astConstructor = new ASTConstructor();
    }

    @Override
    public Node parse(Reader reader) throws IOException, SyntaxErrorException {
        CalcParser.ProgramContext programTree = parseTree(reader);
        return constructAST(programTree);
    }

    protected Node constructAST(CalcParser.ProgramContext programTree) {
        return astConstructor.visit(programTree);
    }

    protected CalcParser.ProgramContext parseTree(
            Reader reader) throws IOException, SyntaxErrorException {
        try (ListenerClosable<SyntaxErrorException> ignored =
                     collectDiagnosticsToThrow(SyntaxErrorException::new)) {
            Lexer lexer = createLexer(reader);
            CalcParser parser = createParser(lexer);

            return parser.program();

        } catch (RecognitionException e) {
            /* Should not happen, unless someone overrides the default error recovery strategy */
            throw new SyntaxErrorException(e);
        }
    }

    protected <T extends Recognizer> T initRecognizer(T recognizer) {
        recognizer.removeErrorListeners();
        recognizer.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                    int line, int charPositionInLine, String msg,
                                    RecognitionException e) {
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
                emitDiagnostic(new Diagnostic(location, msg));
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

}

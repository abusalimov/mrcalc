package com.abusalimov.mrcalc.parse.impl.antlr;

import com.abusalimov.mrcalc.ast.ProgramNode;
import com.abusalimov.mrcalc.diagnostic.AbstractDiagnosticEmitter;
import com.abusalimov.mrcalc.diagnostic.Diagnostic;
import com.abusalimov.mrcalc.location.Location;
import com.abusalimov.mrcalc.location.RawLocation;
import com.abusalimov.mrcalc.parse.Parser;
import com.abusalimov.mrcalc.parse.SyntaxErrorException;
import com.abusalimov.mrcalc.parse.TokenSpan;
import org.antlr.v4.runtime.*;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Eldar Abusalimov
 */
public class ANTLRParserImpl extends AbstractDiagnosticEmitter implements Parser {

    private ASTConstructor astConstructor;

    public ANTLRParserImpl() {
        astConstructor = new ASTConstructor();
    }

    @Override
    public List<TokenSpan> tokenize(Reader reader) throws IOException {
        Lexer lexer = new CalcLexer(new ANTLRInputStream(reader));
        lexer.removeErrorListeners();
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        tokenStream.fill();
        List<Token> antlrTokens = tokenStream.getTokens();
        List<TokenSpan> tokens = new ArrayList<>(antlrTokens.size());

        for (Token antlrToken : antlrTokens) {
            tokens.add(new TokenSpan.Simple(mapTokenKind(antlrToken.getType()), new TokenLocation(antlrToken)));
        }

        return tokens;
    }

    private TokenSpan.Kind mapTokenKind(int antlrTokenType) {
        switch (antlrTokenType) {
            case CalcLexer.WS:
            case CalcLexer.STMT_DELIM:
                return TokenSpan.Kind.WHITESPACE;
            case CalcLexer.EQ_SIGN:
            case CalcLexer.ADD_OP:
            case CalcLexer.SUB_OP:
            case CalcLexer.MUL_OP:
            case CalcLexer.DIV_OP:
            case CalcLexer.POW_OP:
            case CalcLexer.L_PAREN:
            case CalcLexer.R_PAREN:
            case CalcLexer.L_BRACE:
            case CalcLexer.R_BRACE:
            case CalcLexer.COMMA:
            case CalcLexer.ARROW:
                return TokenSpan.Kind.PUNCTUATION;
            case CalcLexer.VAR_KW:
            case CalcLexer.PRINT_KW:
            case CalcLexer.OUT_KW:
                return TokenSpan.Kind.KEYWORD;
            case CalcLexer.MAP_KW:
            case CalcLexer.REDUCE_KW:
                return TokenSpan.Kind.FUNCTION;
            case CalcLexer.ID:
                return TokenSpan.Kind.IDENTIFIER;
            case CalcLexer.INT:
                return TokenSpan.Kind.LITERAL_INTEGER;
            case CalcLexer.FLOAT:
                return TokenSpan.Kind.LITERAL_FLOAT;
            case CalcLexer.STRING:
                return TokenSpan.Kind.LITERAL_STRING;
            case Lexer.EOF:
                return TokenSpan.Kind.EOF;
            default:
                return TokenSpan.Kind.ERROR;
        }
    }

    @Override
    public ProgramNode parse(Reader reader) throws IOException, SyntaxErrorException {
        CalcParser.ProgramContext programTree = parseTree(reader);
        return constructAST(programTree);
    }

    protected ProgramNode constructAST(CalcParser.ProgramContext programTree) {
        return (ProgramNode) astConstructor.visit(programTree);
    }

    protected CalcParser.ProgramContext parseTree(
            Reader reader) throws IOException, SyntaxErrorException {
        try (DiagnosticCollectorCloseable<SyntaxErrorException> ignored =
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

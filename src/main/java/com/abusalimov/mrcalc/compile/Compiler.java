package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.SyntaxErrorException;
import com.abusalimov.mrcalc.ast.Node;
import com.abusalimov.mrcalc.ast.expr.ExprNode;
import com.abusalimov.mrcalc.grammar.CalcLexer;
import com.abusalimov.mrcalc.grammar.CalcParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Objects;

/**
 * @author Eldar Abusalimov
 */
public class Compiler {
    public Code compile(String s) throws SyntaxErrorException {
        try {
            return compile(new StringReader(Objects.requireNonNull(s)));
        } catch (IOException e) {
            /* StringReader never throws for non-null strings, relax. */
            throw new RuntimeException(e);
        }
    }

    public Code compile(Reader reader) throws IOException, SyntaxErrorException {
        final int[] numberOfErrors = {0};
        ANTLRErrorListener errorListener = new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                                    int charPositionInLine, String msg, RecognitionException e) {
                /*
                 * Parser instances have a nice getNumberOfSyntaxErrors() method that could be
                 * used to tell whether there were any syntax errors, but unfortunately Lexers
                 * don't have such method. Hence this workaround...
                 */
                numberOfErrors[0]++;
            }
        };

        Lexer lexer = createLexer(reader);
        lexer.addErrorListener(errorListener);

        CalcParser parser = createParser(lexer);
        parser.addErrorListener(errorListener);

        ParseTree programTree = parser.program();

        if (numberOfErrors[0] > 0) {
            throw new SyntaxErrorException(String.format("%d syntax error(s)", numberOfErrors[0]));
        }

        Node program = new ASTConstructor().visit(programTree);
        return new Code((ExprNode) program);  // FIXME cast
    }

    protected CalcParser createParser(Lexer lexer) {
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        return createParser(tokenStream);
    }

    protected CalcParser createParser(CommonTokenStream tokenStream) {
        return new CalcParser(tokenStream);
    }

    protected Lexer createLexer(Reader reader) throws IOException {
        ANTLRInputStream input = new ANTLRInputStream(reader);
        return createLexer(input);
    }

    protected Lexer createLexer(CharStream input) {
        return new CalcLexer(input);
    }

}

package com.abusalimov.mrcalc;

import com.abusalimov.mrcalc.ast.LiteralNode;
import com.abusalimov.mrcalc.ast.Node;
import com.abusalimov.mrcalc.ast.NodeVisitor;
import com.abusalimov.mrcalc.grammar.CalcLexer;
import com.abusalimov.mrcalc.grammar.CalcParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * @author Eldar Abusalimov
 */
public class Interpreter implements NodeVisitor<Number> {
    @Override
    public Number doVisit(LiteralNode node) {
        return (Number) node.getValue();
    }

    public Number eval(String s) throws IOException, SyntaxErrorException {
        return eval(new StringReader(s));
    }

    public Number eval(Reader reader) throws IOException, SyntaxErrorException {
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

        ANTLRInputStream input = new ANTLRInputStream(reader);
        CalcLexer lexer = new CalcLexer(input);
        lexer.addErrorListener(errorListener);

        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        CalcParser parser = new CalcParser(tokenStream);
        parser.addErrorListener(errorListener);

        ParseTree programTree = parser.program();

        if (numberOfErrors[0] > 0) {
            throw new SyntaxErrorException(String.format("%d syntax error(s)", numberOfErrors[0]));
        }

        Node program = new ASTConstructor().visit(programTree);
        return visit(program);
    }
}

package com.abusalimov.mrcalc;

import com.abusalimov.mrcalc.grammar.CalcLexer;
import com.abusalimov.mrcalc.grammar.CalcParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * @author Eldar Abusalimov
 */
public class Interpreter {

    public String eval(String s) throws IOException {
        return eval(new StringReader(s));
    }

    public String eval(Reader reader) throws IOException {
        ANTLRInputStream input = new ANTLRInputStream(reader);
        CalcLexer lexer = new CalcLexer(input);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        CalcParser parser = new CalcParser(tokenStream);

        ParseTree programTree = parser.program();
        return programTree.toStringTree(parser);
    }
}

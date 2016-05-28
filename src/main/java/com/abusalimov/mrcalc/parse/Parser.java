package com.abusalimov.mrcalc.parse;

import com.abusalimov.mrcalc.ast.Node;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Objects;

/**
 * Parser analyzes input source code and constructs an AST.
 *
 * @author Eldar Abusalimov
 */
public interface Parser {

    Node parse(Reader reader) throws IOException, SyntaxErrorException;

    default Node parse(String s) throws SyntaxErrorException {
        try {
            return parse(new StringReader(Objects.requireNonNull(s)));
        } catch (IOException e) {
            /* StringReader never throws for non-null strings, relax. */
            throw new RuntimeException(e);
        }
    }
}

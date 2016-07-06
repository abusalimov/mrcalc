package com.abusalimov.mrcalc.parse;

import com.abusalimov.mrcalc.ast.ProgramNode;
import com.abusalimov.mrcalc.diagnostic.DiagnosticEmitter;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Parser analyzes input source code and constructs an AST rooted by a {@link ProgramNode}, emitting
 * appropriate {@link com.abusalimov.mrcalc.diagnostic.Diagnostic}s whenever a syntax error occurs.
 *
 * @author Eldar Abusalimov
 */
public interface Parser extends DiagnosticEmitter {

    /**
     * Parses the input specified as a given reader and build the AST.
     *
     * @param reader the source code
     * @return the AST root
     * @throws IOException          in case the specified reader encounters an error
     * @throws SyntaxErrorException in case of malformed input
     */
    ProgramNode parse(Reader reader) throws IOException, SyntaxErrorException;

    /**
     * Parses a given string.
     *
     * @see #parse(Reader)
     */
    default ProgramNode parse(String s) throws SyntaxErrorException {
        try {
            return parse(new StringReader(Objects.requireNonNull(s)));
        } catch (IOException e) {
            /* StringReader never throws for non-null strings, relax. */
            throw new RuntimeException(e);
        }
    }

    /**
     * Tokenizes the input specified as a given reader and build the AST.
     *
     * @param reader the source code
     * @return the AST root
     * @throws IOException in case the specified reader encounters an error
     */
    default List<TokenSpan> tokenize(Reader reader) throws IOException {
        return Collections.emptyList();
    }

    /**
     * Tokenizes a given string.
     *
     * @see #tokenize(Reader)
     */
    default List<TokenSpan> tokenize(String s) {
        try {
            return tokenize(new StringReader(Objects.requireNonNull(s)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

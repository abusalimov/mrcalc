package com.abusalimov.mrcalc.ui;

import com.abusalimov.mrcalc.location.Location;
import com.abusalimov.mrcalc.parse.Parser;
import com.abusalimov.mrcalc.parse.TokenSpan;
import com.abusalimov.mrcalc.parse.impl.antlr.ANTLRParserImpl;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMakerBase;
import org.fife.ui.rsyntaxtextarea.TokenTypes;

import javax.swing.text.Segment;
import java.io.CharArrayReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Eldar Abusalimov
 */
public class TokenMakerImpl extends TokenMakerBase {
    private final Parser parser;

    /**
     * Default constructor to let RSTA instantiate it reflectively.
     */
    public TokenMakerImpl() {
        this(new ANTLRParserImpl());
    }

    public TokenMakerImpl(Parser parser) {
        this.parser = parser;
    }

    @Override
    public Token getTokenList(Segment text, int initialTokenType, int startOffset) {
        List<TokenSpan> tokenList;
        try {
            CharArrayReader reader = new CharArrayReader(text.array, text.offset, text.count);
            tokenList = parser.tokenize(reader);
        } catch (IOException e) {
            e.printStackTrace();
            tokenList = Collections.emptyList();
        }

        resetTokenList();

        for (TokenSpan tokenSpan : tokenList) {
            if (tokenSpan.getKind() == TokenSpan.Kind.EOF)
                break;
            Location location = tokenSpan.getLocation();
            int tokenStartOffset = text.offset + location.getColumnNumber();
            int tokenEndOffset = tokenStartOffset + location.getEndOffset() - location.getStartOffset() - 1;
            addToken(text.array, tokenStartOffset, tokenEndOffset,
                    mapTokenType(tokenSpan.getKind()),
                    startOffset + location.getColumnNumber());
        }

        if (firstToken == null) {
            addNullToken();
        }

        return firstToken;
    }

    private int mapTokenType(TokenSpan.Kind kind) {
        switch (kind) {
            case WHITESPACE:
                return TokenTypes.WHITESPACE;
            case COMMENT:
                return TokenTypes.COMMENT_EOL;
            case KEYWORD:
                return TokenTypes.RESERVED_WORD;
            case PUNCTUATION:
                return TokenTypes.SEPARATOR;
            case IDENTIFIER:
                return TokenTypes.IDENTIFIER;
            case FUNCTION:
                return TokenTypes.FUNCTION;
            case LITERAL_INTEGER:
                return TokenTypes.LITERAL_NUMBER_DECIMAL_INT;
            case LITERAL_FLOAT:
                return TokenTypes.LITERAL_NUMBER_FLOAT;
            case LITERAL_STRING:
                return TokenTypes.LITERAL_STRING_DOUBLE_QUOTE;
            case EOF:
                return TokenTypes.NULL;
            case ERROR:
            default:
                return TokenTypes.ERROR_CHAR;
        }
    }
}

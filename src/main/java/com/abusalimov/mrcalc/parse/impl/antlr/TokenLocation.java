package com.abusalimov.mrcalc.parse.impl.antlr;

import com.abusalimov.mrcalc.location.Location;
import org.antlr.v4.runtime.Token;

import java.util.Objects;

/**
 * The {@link Location} implementation backed by a single ANTLR token.
 *
 * @author Eldar Abusalimov
 */
public class TokenLocation extends AbstractLocation implements Location {
    private final Token token;

    public TokenLocation(Token token) {
        this.token = Objects.requireNonNull(token);
    }

    public Token getToken() {
        return token;
    }

    @Override
    public Token getStartToken() {
        return token;
    }

    @Override
    public Token getStopToken() {
        return token;
    }
}

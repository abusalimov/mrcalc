package com.abusalimov.mrcalc.parse;

import com.abusalimov.mrcalc.location.Location;

/**
 * @author Eldar Abusalimov
 */
public interface TokenSpan {
    Kind getKind();

    Location getLocation();

    enum Kind {
        WHITESPACE,
        COMMENT,
        KEYWORD,
        PUNCTUATION,
        IDENTIFIER,
        FUNCTION,
        LITERAL_INTEGER,
        LITERAL_FLOAT,
        LITERAL_STRING,
        EOF,
        ERROR
    }

    /**
     * Simple and straightforward implementation.
     */
    class Simple implements TokenSpan {
        private final Kind kind;
        private final Location location;

        public Simple(Kind kind, Location location) {
            this.kind = kind;
            this.location = location;
        }

        @Override
        public Kind getKind() {
            return kind;
        }

        @Override
        public Location getLocation() {
            return location;
        }
    }
}

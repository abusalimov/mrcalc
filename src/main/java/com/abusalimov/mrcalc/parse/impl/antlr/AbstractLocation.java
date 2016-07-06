package com.abusalimov.mrcalc.parse.impl.antlr;

import com.abusalimov.mrcalc.location.Location;
import org.antlr.v4.runtime.Token;

/**
 * Implements most of the {@link Location} methods by delegating to backing ANTLR {@link Token}
 * instances, which in turn are provided by subclasses of this class.
 * <p>
 * <pre>{@code
 *
 *   TokenSpan:   |F I R S T  ...  L A S T|
 *            |                       |
 *            `- startOffset          `- stopOffset
 *
 * }</pre>
 *
 * @author Eldar Abusalimov
 */
public abstract class AbstractLocation implements Location {
    @Override
    public int getLineNumber() {
        return getStartToken().getLine();
    }

    @Override
    public int getColumnNumber() {
        return getStartToken().getCharPositionInLine();
    }

    @Override
    public int getOffset() {
        return getStartOffset();
    }

    @Override
    public int getStartOffset() {
        return getStartToken().getStartIndex();
    }

    @Override
    public int getEndOffset() {
        /* The token.getStopIndex() method returns the index of the last char (inclusive),
         * but we want the index past that char (i.e. exclusive).
         * Zero-length tokens like EOF have their stopIndex < startOffset, and EOF at the very
         * beginning will have its stopIndex == -1, like a token with no location info at all.
         * This is likely a subtle ANTLR bug, which is easy to workaround. */
        int stopIndex = getStopToken().getStopIndex();
        if (stopIndex >= 0 || getStopToken().getStartIndex() >= 0) {
            stopIndex += 1;
        }
        return stopIndex;
    }

    /**
     * Returns the first token of the location span.
     *
     * @return the non-{@code null} backing token instance
     */
    public abstract Token getStopToken();

    /**
     * Returns the last token of the location span.
     *
     * @return the non-{@code null} backing token instance
     */
    public abstract Token getStartToken();
}

package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.location.Location;
import org.antlr.v4.runtime.Token;

/**
 * Implements most of the {@link Location} methods by delegating to backing ANTLR {@link Token}
 * instances, which in turn are provided by subclasses of this class.
 * <p>
 * <pre>{@code
 *
 *   Token:   |F I R S T  ...  L A S T|
 *            |                       |
 *            `- startOffset          `- stopOffset
 *
 * }</pre>
 *
 * @author Eldar Abusalimov
 */
public abstract class AbstractANTLRLocation implements Location {
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
         * but we want the index past that char (i.e. exclusive). */
        int stopIndex = getStopToken().getStopIndex();
        if (stopIndex >= 0) {
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

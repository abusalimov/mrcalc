package com.abusalimov.mrcalc.location;

/**
 * Location refers to a position within source code.
 *
 * @author Eldar Abusalimov
 */
public interface Location {
    /**
     * Singleton instance indicating an unknown location.
     */
    Location UNKNOWN_LOCATION = new RawLocation(-1, -1, -1, -1, -1);

    /**
     * Returns a 1-based number of the line in a source code.
     *
     * @return the line number from {@code [1..n]} range, or {@code -1} if the position is unknown
     */
    int getLineNumber();

    /**
     * Returns a 0-based number of the column within a source line.
     *
     * @return the column number from {@code [0..n-1]} range, or {@code -1} if the position is
     * unknown
     */
    int getColumnNumber();

    /**
     * Returns the position within the source code buffer.
     *
     * @return offset from the beginning of the buffer, or {@code -1} if unknown
     */
    int getOffset();

    /**
     * Returns the position within the source code buffer indicating the span start, if any, such
     * that the {@code getStartOffset() <= getOffset()} invariant is held.
     *
     * @return offset from the beginning of the buffer, or {@code -1} if unknown
     */
    int getStartOffset();

    /**
     * Returns the position within the source code buffer indicating the span end, if any, such that
     * the {@code getOffset() <= getEndOffset()} invariant is held.
     *
     * @return offset from the beginning of the buffer, or {@code -1} if unknown
     */
    int getEndOffset();
}

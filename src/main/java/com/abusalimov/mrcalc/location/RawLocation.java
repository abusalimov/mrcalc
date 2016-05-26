package com.abusalimov.mrcalc.location;

/**
 * Simple implementation of the {@link Location} interface.
 *
 * @author Eldar Abusalimov
 */
public class RawLocation implements Location {
    private final int lineNumber;
    private final int columnNumber;
    private final int offset;
    private final int startOffset;
    private final int endOffset;

    /**
     * Creates a new immutable instance with given properties.
     *
     * @param lineNumber   a line number to return as {@link #getLineNumber()}
     * @param columnNumber a column number to return as {@link #getColumnNumber()}
     * @param offset       an offset value to return as {@link #getOffset()}
     * @param startOffset  a start offset value to return as {@link #getStartOffset()}
     * @param endOffset    an end offset value to return as {@link #getEndOffset()}
     */
    public RawLocation(int lineNumber, int columnNumber, int offset, int startOffset,
                       int endOffset) {
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
        this.offset = offset;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    @Override
    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public int getColumnNumber() {
        return columnNumber;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public int getStartOffset() {
        return startOffset;
    }

    @Override
    public int getEndOffset() {
        return endOffset;
    }
}

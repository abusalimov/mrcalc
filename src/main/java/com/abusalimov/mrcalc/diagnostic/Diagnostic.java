package com.abusalimov.mrcalc.diagnostic;

import com.abusalimov.mrcalc.location.Location;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * Diagnostic represents an error found during certain phase of processing the source code. Whenever
 * a syntax/type/runtime error occurs, a new Diagnostic instance is emitted to all {@link
 * DiagnosticListener}s attached to the parser/compiler/interpreter. After finishing processing, a
 * {@link DiagnosticException} may be thrown in case there's at least one issue to report.
 *
 * @author Eldar Abusalimov
 */
public class Diagnostic {
    private final Location location;
    private final String message;

    /**
     * Creates a new Diagnostic with a given {@link Location} and message string.
     *
     * @param location a non-{@code null} Location instance
     * @param message  the description of the issue
     */
    public Diagnostic(Location location, String message) {
        this.location = Objects.requireNonNull(location);
        this.message = message;
    }

    /**
     * Returns the {@link Location} info attached to the issue.
     *
     * @return a non-{@code null} Location instance
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Returns the description of the issue.
     *
     * @return the description string
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return a caret line with no additional offset
     * @see #getCaretLine(int)
     */
    public String getCaretLine() {
        return getCaretLine(0);
    }

    /**
     * Constructs a line with a `^` caret at the proper column.
     *
     * @param columnOffset an optional offset from the line start
     * @return a string containing a `^` caret, or an empty one if the {@link Location} attached to
     * the issue doesn't provide {@link Location#getColumnNumber() column info}
     */
    public String getCaretLine(int columnOffset) {
        int columnNumber = location.getColumnNumber();
        if (columnNumber < 0) {
            return "";
        }
        int column = columnOffset + columnNumber;
        int length = Math.max(1, location.getEndOffset() - location.getStartOffset());
        return StringUtils.repeat(' ', column) + StringUtils.repeat('^', length);
    }

    /**
     * Formats a "line no:col" string.
     *
     * @return a string with line/column number info, or an empty one in case the attached {@link
     * Location} doesn't provide the necessary info
     */
    public String getLinePrefix() {
        int lineNumber = location.getLineNumber();
        int columnNumber = location.getColumnNumber();
        if (lineNumber < 0 || columnNumber < 0) {
            return "";
        }
        return String.format("line %d:%d", lineNumber, columnNumber);
    }

    @Override
    public String toString() {
        String message = getMessage();
        if (message == null) {
            message = "<unknown>";
        }
        String linePrefix = getLinePrefix();
        if (linePrefix.length() > 0) {
            return String.format("%s: %s", linePrefix, message);
        } else {
            return message;
        }
    }
}

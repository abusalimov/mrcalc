package com.abusalimov.mrcalc.ui;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author - Eldar Abusalimov
 */
public class TextAreaStream extends OutputStream {
    private static final int MAX_BUFFER_SIZE = 256;
    private static final int BUFFER_WRAP_GAP = 32;

    private StringBuilder buffer;
    private JTextArea textArea;
    private boolean closed;

    public TextAreaStream(JTextArea textArea) {
        buffer = new StringBuilder(MAX_BUFFER_SIZE);
        this.textArea = textArea;
    }

    @Override
    public void write(int b) throws IOException {
        if (closed)
            throw new IOException("Stream is closed");

        char ch = (char) b;
        int len = buffer.length();

        if (len >= MAX_BUFFER_SIZE - BUFFER_WRAP_GAP) {
            char prevCh = buffer.charAt(len - 1);
            boolean isWordStart = (!Character.isLetterOrDigit(prevCh) && Character.isLetterOrDigit(ch));
            if (isWordStart || len >= MAX_BUFFER_SIZE) {
                buffer.append("↵\n");
                flush();
            }
        }

        buffer.append(ch);
    }

    @Override
    public void flush() throws IOException {
        if (closed)
            throw new IOException("Stream is closed");

        appendText(buffer.toString());
        buffer.delete(0, buffer.length());
    }

    @Override
    public void close() throws IOException {
        flush();
        closed = true;
    }

    private void appendText(String string) {
        if (EventQueue.isDispatchThread()) {
            textArea.append(string);
            textArea.setCaretPosition(textArea.getText().length());
        } else {
            EventQueue.invokeLater(() -> appendText(string));
        }
    }
}

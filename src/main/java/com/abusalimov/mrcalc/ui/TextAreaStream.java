package com.abusalimov.mrcalc.ui;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author - Eldar Abusalimov
 */
public class TextAreaStream extends OutputStream {
    private StringBuilder buffer;
    private JTextArea textArea;

    public TextAreaStream(JTextArea textArea) {
        buffer = new StringBuilder(128);
        this.textArea = textArea;
    }

    @Override
    public void write(int b) throws IOException {
        char c = (char) b;
        String value = Character.toString(c);
        buffer.append(value);
        if (value.equals("\n")) {
            flush();
        }
    }

    @Override
    public void flush() throws IOException {
        appendText(buffer.toString());
        buffer.delete(0, buffer.length());
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

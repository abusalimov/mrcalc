package com.abusalimov.mrcalc.ui;

import javax.swing.*;
import java.io.IOException;

/**
 * @author Eldar Abusalimov
 */
public class OutputTextArea extends JTextArea {
    private TextAreaStream textAreaStream;

    public void closeStream() throws IOException {
        if (textAreaStream != null) {
            textAreaStream.close();
        }
        textAreaStream = null;
    }

    public TextAreaStream createStream() {
        try {
            closeStream();
        } catch (IOException ignored) {
        }

        return textAreaStream = new TextAreaStream(this);
    }
}

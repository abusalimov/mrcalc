package com.abusalimov.mrcalc.ui;

import com.abusalimov.mrcalc.diagnostic.Diagnostic;
import com.abusalimov.mrcalc.location.Location;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * @author - Eldar Abusalimov
 */
public class MessageList extends JList<Diagnostic> {
    public MessageList(final JTextPane textPane) {
        ((DefaultCaret)textPane.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isSelectionEmpty() && e.getClickCount() > 1)
                    goToError(textPane);
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (!isSelectionEmpty() && e.getKeyChar() == KeyEvent.VK_ENTER)
                    goToError(textPane);
            }
        });
    }

    private void goToError(JTextPane textPane) {
        Location messageLocation = getSelectedValue().getLocation();
        textPane.setCaretPosition(messageLocation.getEndOffset());
        textPane.requestFocusInWindow();
    }

    public void setMessages(final List<Diagnostic> messages) {
        setModel(new AbstractListModel<Diagnostic>() {
            @Override
            public int getSize() {
                return messages.size();
            }

            @Override
            public Diagnostic getElementAt(int index) {
                return messages.get(index);
            }
        });
    }
}

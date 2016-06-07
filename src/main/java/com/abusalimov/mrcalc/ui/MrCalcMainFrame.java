package com.abusalimov.mrcalc.ui;

import com.abusalimov.mrcalc.diagnostic.Diagnostic;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;

/**
 * @author - Eldar Abusalimov
 */
public class MrCalcMainFrame extends JFrame {
    public static final int PREFERRED_WIDTH = 800;

    private JTextPane codeTextPane;
    private JTextArea outputTextArea;
    private JList<Diagnostic> messageList;

    public MrCalcMainFrame() {
        super("MrCalc");

        codeTextPane = new JTextPane();
        outputTextArea = new JTextArea();
        outputTextArea.setEditable(false);
        messageList = new JList<>();

        StyleContext style = StyleContext.getDefaultStyleContext();

        AttributeSet defaultAttr = style
                .addAttribute(style.getEmptySet(), StyleConstants.FontFamily, "Monospaced");
        defaultAttr = style.addAttribute(defaultAttr, StyleConstants.FontSize, 16);

        codeTextPane.setCharacterAttributes(defaultAttr, true);
        outputTextArea.setFont(new Font("Monospaced", Font.BOLD, 16));

        initLayout();
    }

    private void initLayout() {
        JScrollPane codeScrollPane = new JScrollPane(codeTextPane);

        codeScrollPane.setPreferredSize(new Dimension(PREFERRED_WIDTH, 400));

        JPanel outputPanel = new JPanel();
        outputPanel.setLayout(new BoxLayout(outputPanel, BoxLayout.Y_AXIS));
        JScrollPane outputScrollPane = new JScrollPane(outputTextArea);
        outputPanel.add(new JLabel("Program output"));
        outputPanel.add(outputScrollPane);

        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        JScrollPane messageScrollPane = new JScrollPane(messageList);
        messagePanel.add(new JLabel("Messages"));
        messagePanel.add(messageScrollPane);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, outputPanel,
                messagePanel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(0.3);

        JSplitPane outerSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, codeScrollPane,
                splitPane);
        outerSplitPane.setOneTouchExpandable(true);
        outerSplitPane.setResizeWeight(1.);

        add(outerSplitPane);

        setPreferredSize(new Dimension(PREFERRED_WIDTH, 600));
        pack();
    }

}

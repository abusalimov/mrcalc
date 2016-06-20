package com.abusalimov.mrcalc.ui;

import com.abusalimov.mrcalc.CalcExecutor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;

/**
 * @author - Eldar Abusalimov
 */
public class MrCalcMainFrame extends JFrame {
    public static final int PREFERRED_WIDTH = 800;

    private final BackendTypeSwitcher backendTypeSwitcher;
    private final CodeTextPane codeTextPane;
    private final OutputTextArea outputTextArea;
    private final MessageList messageList;

    public MrCalcMainFrame(CalcExecutor calcExecutor) {
        super("MrCalc");

        backendTypeSwitcher = new BackendTypeSwitcher(calcExecutor);
        outputTextArea = new OutputTextArea();
        outputTextArea.setEditable(false);
        codeTextPane = new CodeTextPane(calcExecutor, outputTextArea);
        messageList = new MessageList(codeTextPane);
        codeTextPane.setErrorListener(messageList::setMessages);

        StyleContext style = StyleContext.getDefaultStyleContext();

        AttributeSet defaultAttr = style
                .addAttribute(style.getEmptySet(), StyleConstants.FontFamily, "Monospaced");
        defaultAttr = style.addAttribute(defaultAttr, StyleConstants.FontSize, 16);

        codeTextPane.setCharacterAttributes(defaultAttr, true);
        outputTextArea.setFont(new Font("Monospaced", Font.BOLD, 16));
        outputTextArea.setLineWrap(true);
        outputTextArea.setWrapStyleWord(true);

        initLayout();
    }

    private void initLayout() {
        JScrollPane codeScrollPane = new JScrollPane(codeTextPane);

        JPanel toolPanel = new JPanel();
        toolPanel.setBorder(new EmptyBorder(0,3,0,3));
        toolPanel.setLayout(new BorderLayout());
        backendTypeSwitcher.setBorder(new TitledBorder("Backend"));
        toolPanel.add(backendTypeSwitcher, BorderLayout.NORTH);

        TextLineNumber tln = new TextLineNumber(codeTextPane);
        codeScrollPane.setRowHeaderView(tln);
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

        JPanel upperPanel = new JPanel(new BorderLayout());
        upperPanel.setBorder(new EmptyBorder(3,3,3,3));
        upperPanel.add(codeScrollPane, BorderLayout.CENTER);
        upperPanel.add(toolPanel, BorderLayout.EAST);

        JSplitPane outerSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperPanel,
                splitPane);
        outerSplitPane.setOneTouchExpandable(true);
        outerSplitPane.setResizeWeight(1.);

        add(outerSplitPane);

        outerSplitPane.setPreferredSize(new Dimension(PREFERRED_WIDTH, 600));
        pack();
    }

}

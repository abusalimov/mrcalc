package com.abusalimov.mrcalc;

import com.abusalimov.mrcalc.ui.MrCalcMainFrame;

import javax.swing.*;

/**
 * @author Eldar Abusalimov
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MrCalcMainFrame mainFrame = new MrCalcMainFrame(new CalcExecutor());
            mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            mainFrame.setVisible(true);
        });
    }
}

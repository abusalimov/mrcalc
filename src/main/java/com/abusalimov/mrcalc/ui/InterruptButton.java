package com.abusalimov.mrcalc.ui;

import com.abusalimov.mrcalc.CalcExecutor;

import javax.swing.*;

/**
 * @author Eldar Abusalimov
 */
public class InterruptButton extends JButton {
    public InterruptButton(CalcExecutor calcExecutor) {
        super("Interrupt");
        addActionListener(e -> calcExecutor.cancel());
        calcExecutor.addAndFireExecutionListener(this::setEnabled);
    }
}

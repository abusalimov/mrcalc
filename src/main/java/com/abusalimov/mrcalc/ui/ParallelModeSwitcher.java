package com.abusalimov.mrcalc.ui;

import com.abusalimov.mrcalc.CalcExecutor;

import javax.swing.*;

/**
 * @author Eldar Abusalimov
 */
public class ParallelModeSwitcher extends JCheckBox {
    public ParallelModeSwitcher(CalcExecutor executor) {
        super("Parallel");

        setSelected(executor.isParallel());
        addActionListener(a -> executor.setParallel(isSelected()));
    }
}

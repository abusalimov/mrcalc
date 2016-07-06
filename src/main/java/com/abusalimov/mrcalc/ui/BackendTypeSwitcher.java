package com.abusalimov.mrcalc.ui;

import com.abusalimov.mrcalc.CalcExecutor;

import javax.swing.*;

/**
 * Radio buttons for selecting {@link com.abusalimov.mrcalc.backend.Backend} implementation.
 *
 * @author Eldar Abusalimov
 */
public class BackendTypeSwitcher extends JPanel {
    public BackendTypeSwitcher(CalcExecutor executor) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        ButtonGroup group = new ButtonGroup();

        for (CalcExecutor.BackendImplSwitch backendImplSwitch : CalcExecutor.BackendImplSwitch.values()) {
            JRadioButton button = new JRadioButton(backendImplSwitch.getName());
            button.addActionListener(a -> executor.setBackendImplSwitch(backendImplSwitch));
            group.add(button);
            add(button);

            if (backendImplSwitch == executor.getBackendImplSwitch()) {
                button.setSelected(true);
            }
        }
    }

}

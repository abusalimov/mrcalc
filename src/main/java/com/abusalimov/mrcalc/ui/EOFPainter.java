package com.abusalimov.mrcalc.ui;

import java.awt.*;

/**
 * @author - Eldar Abusalimov
 */
public class EOFPainter extends SquigglePainter {
    public EOFPainter(Color color) {
        super(color);
    }

    @Override
    protected void paintSquiggles(Graphics g, Rectangle r, int squiggle) {
        super.paintSquiggles(g, new Rectangle(r.x + r.width, r.y, 4*squiggle, r.height), squiggle);
    }
}

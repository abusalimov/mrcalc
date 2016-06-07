package com.abusalimov.mrcalc.ui;

import java.awt.*;
import javax.swing.text.*;

/*
 *  Implements a simple highlight painter that renders a rectangle around the
 *  area to be highlighted.
 *
 */
public class SquigglePainter extends DefaultHighlighter.DefaultHighlightPainter
{
    public SquigglePainter(Color color)
    {
        super( color );
    }

    /**
     * Paints a portion of a highlight.
     *
     * @param  g the graphics context
     * @param  offs0 the starting model offset >= 0
     * @param  offs1 the ending model offset >= offs1
     * @param  bounds the bounding box of the view, which is not
     *	       necessarily the region to paint.
     * @param  c the editor
     * @param  view View painting for
     * @return region drawing occured in
     */
    public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view)
    {
        Rectangle r = getDrawingArea(offs0, offs1, bounds, view);

        if (r == null) return null;

        //  Do your custom painting

        Color color = getColor();
        g.setColor(color == null ? c.getSelectionColor() : color);
//        ((Graphics2D) g).setStroke(new BasicStroke(1.f));

        //  Draw the squiggles

        paintSquiggles(g, r, 2);

        return r;
    }

    protected void paintSquiggles(Graphics g, Rectangle r, int squiggle) {
        int twoSquiggles = squiggle * 2;
        int y = r.y + r.height - squiggle - 1;

        for (int x = r.x; x <= r.x + r.width - twoSquiggles; x += twoSquiggles)
        {
            g.drawLine(x, y, x + squiggle, y + squiggle);
            g.drawLine(x + squiggle, y + squiggle, x + twoSquiggles, y);
        }
    }


    protected Rectangle getDrawingArea(int offs0, int offs1, Shape bounds, View view)
    {
        // Contained in view, can just use bounds.

        if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset())
        {
            Rectangle alloc;

            if (bounds instanceof Rectangle)
            {
                alloc = (Rectangle)bounds;
            }
            else
            {
                alloc = bounds.getBounds();
            }

            return alloc;
        }
        else
        {
            // Should only render part of View.
            try
            {
                // --- determine locations ---
                Shape shape = view.modelToView(offs0, Position.Bias.Forward, offs1,Position.Bias.Backward, bounds);
                Rectangle r = (shape instanceof Rectangle) ? (Rectangle)shape : shape.getBounds();

                return r;
            }
            catch (BadLocationException e)
            {
                // can't render
            }
        }

        // Can't render

        return null;
    }
}
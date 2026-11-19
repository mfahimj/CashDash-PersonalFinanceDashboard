package com.finance.tracker.ui.components;

import com.finance.tracker.ui.ThemeManager;

import javax.swing.*;
import java.awt.*;

public class RoundedPanel extends JPanel {
    private int cornerRadius = 16;
    private Color customBackgroundColor = null;
    private Color customBorderColor = null;
    private boolean drawBorder = true;

    public RoundedPanel() {
        this(16);
    }

    public RoundedPanel(int radius) {
        super();
        this.cornerRadius = radius;
        setOpaque(false);
    }

    public RoundedPanel(LayoutManager layout, int radius) {
        super(layout);
        this.cornerRadius = radius;
        setOpaque(false);
    }

    public void setCustomBackgroundColor(Color color) {
        this.customBackgroundColor = color;
        repaint();
    }

    public void setCustomBorderColor(Color color) {
        this.customBorderColor = color;
        repaint();
    }

    public void setDrawBorder(boolean draw) {
        this.drawBorder = draw;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Background
        Color bg = customBackgroundColor != null ? customBackgroundColor : ThemeManager.getCardBackgroundColor();
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);

        // Border
        if (drawBorder) {
            Color border = customBorderColor != null ? customBorderColor : ThemeManager.getCardBorderColor();
            g2.setColor(border);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}


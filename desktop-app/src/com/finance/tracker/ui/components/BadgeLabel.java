package com.finance.tracker.ui.components;

import com.finance.tracker.ui.ThemeManager;

import javax.swing.*;
import java.awt.*;

public class BadgeLabel extends JLabel {
    private Color badgeBackground = new Color(241, 245, 249);
    private Color badgeTextColor = new Color(71, 85, 105);

    public BadgeLabel(String text) {
        super(text, SwingConstants.CENTER);
        setFont(ThemeManager.getFontMedium(11f));
        setForeground(badgeTextColor);
        setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        setOpaque(false);
    }

    public void setColors(Color bg, Color text) {
        this.badgeBackground = bg;
        this.badgeTextColor = text;
        setForeground(text);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(badgeBackground);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        g2.dispose();
        super.paintComponent(g);
    }
}


package com.finance.tracker.ui.components;

import com.finance.tracker.ui.ThemeManager;

import javax.swing.*;
import java.awt.*;

public class SearchTextField extends JTextField {
    private String placeholder = "Search...";

    public SearchTextField(String placeholder) {
        this.placeholder = placeholder;
        setFont(ThemeManager.getFontRegular(13f));
        setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        setOpaque(false);
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g2.setColor(ThemeManager.getInputBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

        // Border
        g2.setColor(ThemeManager.getCardBorderColor());
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

        g2.dispose();
        super.paintComponent(g);

        if (getText().isEmpty() && placeholder != null && !placeholder.isEmpty()) {
            Graphics2D g3 = (Graphics2D) g.create();
            g3.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g3.setColor(ThemeManager.getTextMuted());
            g3.setFont(getFont());
            FontMetrics fm = g3.getFontMetrics();
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g3.drawString(placeholder, 12, y);
            g3.dispose();
        }
    }
}


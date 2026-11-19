package com.finance.tracker.ui.components;

import com.finance.tracker.ui.ThemeManager;

import javax.swing.*;
import java.awt.*;

public class MetricCard extends RoundedPanel {
    private final JLabel titleLabel;
    private final JLabel valueLabel;
    private final JLabel subtextLabel;
    private final JLabel iconLabel;
    private JProgressBar progressBar;
    private boolean showProgressBar = false;

    public MetricCard(String title, String initialValue, String subtext, String iconSymbol, Color iconBg) {
        super(new BorderLayout(12, 8), 16);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Top Row: Title + Icon Badge
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        titleLabel = new JLabel(title);
        titleLabel.setFont(ThemeManager.getFontMedium(13f));
        titleLabel.setForeground(ThemeManager.getTextSecondary());
        topPanel.add(titleLabel, BorderLayout.WEST);

        iconLabel = new JLabel(iconSymbol, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(iconBg != null ? iconBg : ThemeManager.getIncomeLightBg());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconLabel.setPreferredSize(new Dimension(32, 32));
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        iconLabel.setForeground(iconBg != null ? iconBg.darker() : ThemeManager.getIncomeColor());
        topPanel.add(iconLabel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Center: Big Bold Value
        valueLabel = new JLabel(initialValue);
        valueLabel.setFont(ThemeManager.getFontBold(24f));
        valueLabel.setForeground(ThemeManager.getTextPrimary());
        add(valueLabel, BorderLayout.CENTER);

        // Bottom: Subtitle / Comparison / Progress Bar
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 6));
        bottomPanel.setOpaque(false);

        subtextLabel = new JLabel(subtext);
        subtextLabel.setFont(ThemeManager.getFontRegular(12f));
        subtextLabel.setForeground(ThemeManager.getTextSecondary());
        bottomPanel.add(subtextLabel, BorderLayout.NORTH);

        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(100, 8));
        progressBar.setVisible(false);
        progressBar.setForeground(ThemeManager.getPrimaryAccent());
        bottomPanel.add(progressBar, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        ThemeManager.addThemeListener(this::updateThemeColors);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setSubtext(String subtext, Color color) {
        subtextLabel.setText(subtext);
        if (color != null) {
            subtextLabel.setForeground(color);
        } else {
            subtextLabel.setForeground(ThemeManager.getTextSecondary());
        }
    }

    public void enableProgressBar(int percent, Color barColor) {
        showProgressBar = true;
        progressBar.setVisible(true);
        progressBar.setValue(Math.min(100, Math.max(0, percent)));
        if (barColor != null) {
            progressBar.setForeground(barColor);
        }
    }

    private void updateThemeColors() {
        titleLabel.setForeground(ThemeManager.getTextSecondary());
        valueLabel.setForeground(ThemeManager.getTextPrimary());
        repaint();
    }
}


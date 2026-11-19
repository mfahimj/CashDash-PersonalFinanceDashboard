package com.finance.tracker.ui.charts;

import com.finance.tracker.model.Budget;
import com.finance.tracker.ui.ThemeManager;
import com.finance.tracker.ui.components.RoundedPanel;
import com.finance.tracker.util.CurrencyFormatter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BudgetProgressMeter extends RoundedPanel {
    private List<Budget> budgets = new ArrayList<>();

    public BudgetProgressMeter() {
        super(16);
        setPreferredSize(new Dimension(460, 260));
        ThemeManager.addThemeListener(this::repaint);
    }

    public void setBudgets(List<Budget> budgets) {
        this.budgets = budgets != null ? budgets : new ArrayList<>();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Title
        g2.setFont(ThemeManager.getFontBold(14f));
        g2.setColor(ThemeManager.getTextPrimary());
        g2.drawString("Budget Performance", 20, 26);

        if (budgets.isEmpty()) {
            g2.setColor(ThemeManager.getTextMuted());
            g2.setFont(ThemeManager.getFontRegular(13f));
            g2.drawString("No budgets configured for this month.", 30, height / 2);
            g2.dispose();
            return;
        }

        int startY = 55;
        int maxItems = Math.min(5, budgets.size());
        int itemHeight = 38;
        int barWidth = width - 40;

        for (int i = 0; i < maxItems; i++) {
            Budget b = budgets.get(i);
            int y = startY + (i * itemHeight);

            // Category Name
            g2.setFont(ThemeManager.getFontMedium(12f));
            g2.setColor(ThemeManager.getTextPrimary());
            String catName = b.getCategoryName() != null ? b.getCategoryName() : "Overall";
            g2.drawString(catName, 20, y);

            // Spent vs Limit string
            String ratioStr = String.format("%s / %s (%.1f%%)",
                    CurrencyFormatter.format(b.getSpent()),
                    CurrencyFormatter.format(b.getAmount()),
                    b.getPercentage());
            g2.setFont(ThemeManager.getFontRegular(11f));
            g2.setColor(ThemeManager.getTextSecondary());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(ratioStr, width - 20 - fm.stringWidth(ratioStr), y);

            // Progress Bar Track
            int barY = y + 8;
            g2.setColor(ThemeManager.getTableAlternateRow());
            g2.fillRoundRect(20, barY, barWidth, 8, 4, 4);

            // Progress Fill
            double pct = Math.min(100.0, Math.max(0.0, b.getPercentage()));
            int fillWidth = (int) ((pct / 100.0) * barWidth);

            Color barColor = ThemeManager.getIncomeColor();
            if (b.getPercentage() >= 100.0) {
                barColor = ThemeManager.getExpenseColor();
            } else if (b.getPercentage() >= 80.0) {
                barColor = ThemeManager.getWarningColor();
            }

            g2.setColor(barColor);
            g2.fillRoundRect(20, barY, fillWidth, 8, 4, 4);
        }

        g2.dispose();
    }
}


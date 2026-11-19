package com.finance.tracker.ui.charts;

import com.finance.tracker.service.FinanceService.MonthlyReportItem;
import com.finance.tracker.ui.ThemeManager;
import com.finance.tracker.ui.components.RoundedPanel;
import com.finance.tracker.util.CurrencyFormatter;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TrendLineChartPanel extends RoundedPanel {
    private List<MonthlyReportItem> data = new ArrayList<>();

    public TrendLineChartPanel() {
        super(16);
        setPreferredSize(new Dimension(500, 240));
        ThemeManager.addThemeListener(this::repaint);
    }

    public void setData(List<MonthlyReportItem> data) {
        this.data = data != null ? data : new ArrayList<>();
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
        g2.drawString("Monthly Spending Trends & Savings", 20, 26);

        // Legend: Expense Trend Line (Violet), Savings Trend (Cyan)
        int legendX = width - 260;
        g2.setColor(new Color(139, 92, 246));
        g2.fillOval(legendX, 16, 10, 10);
        g2.setFont(ThemeManager.getFontMedium(11f));
        g2.setColor(ThemeManager.getTextSecondary());
        g2.drawString("Expense Trend", legendX + 16, 25);

        g2.setColor(new Color(6, 182, 212));
        g2.fillOval(legendX + 120, 16, 10, 10);
        g2.setColor(ThemeManager.getTextSecondary());
        g2.drawString("Savings", legendX + 136, 25);

        if (data.isEmpty()) {
            g2.setColor(ThemeManager.getTextMuted());
            g2.drawString("No trend data available.", 30, height / 2);
            g2.dispose();
            return;
        }

        int paddingLeft = 50;
        int paddingRight = 30;
        int paddingTop = 50;
        int paddingBottom = 40;
        int chartWidth = width - paddingLeft - paddingRight;
        int chartHeight = height - paddingTop - paddingBottom;

        BigDecimal maxVal = BigDecimal.ONE;
        for (MonthlyReportItem item : data) {
            if (item.getExpenses().compareTo(maxVal) > 0) maxVal = item.getExpenses();
            if (item.getSavings().compareTo(maxVal) > 0) maxVal = item.getSavings();
        }
        double maxDouble = maxVal.doubleValue() * 1.2;

        // Grid lines
        g2.setColor(ThemeManager.getCardBorderColor());
        g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 0));
        g2.setFont(ThemeManager.getFontRegular(10f));

        int gridLines = 4;
        for (int i = 0; i <= gridLines; i++) {
            int y = paddingTop + chartHeight - (int) ((double) i / gridLines * chartHeight);
            g2.drawLine(paddingLeft, y, width - paddingRight, y);
            double lineVal = (maxDouble / gridLines) * i;
            g2.setColor(ThemeManager.getTextMuted());
            g2.drawString(String.format("%.0fk", lineVal / 1000), 15, y + 4);
            g2.setColor(ThemeManager.getCardBorderColor());
        }

        int count = data.size();
        if (count < 2) {
            g2.dispose();
            return;
        }

        int step = chartWidth / (count - 1);
        Point[] expPoints = new Point[count];
        Point[] savPoints = new Point[count];

        for (int i = 0; i < count; i++) {
            MonthlyReportItem item = data.get(i);
            int x = paddingLeft + (i * step);

            double ev = Math.max(0, item.getExpenses().doubleValue());
            int ey = paddingTop + chartHeight - (int) ((ev / maxDouble) * chartHeight);
            expPoints[i] = new Point(x, ey);

            double sv = Math.max(0, item.getSavings().doubleValue());
            int sy = paddingTop + chartHeight - (int) ((sv / maxDouble) * chartHeight);
            savPoints[i] = new Point(x, sy);

            // Month Label
            g2.setColor(ThemeManager.getTextSecondary());
            g2.setFont(ThemeManager.getFontMedium(10f));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(item.getMonthLabel(), x - (fm.stringWidth(item.getMonthLabel()) / 2), height - 15);
        }

        // Draw Expense Line and Area
        GeneralPath expPath = new GeneralPath();
        expPath.moveTo(expPoints[0].x, expPoints[0].y);
        for (int i = 1; i < count; i++) {
            expPath.lineTo(expPoints[i].x, expPoints[i].y);
        }

        g2.setColor(new Color(139, 92, 246));
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(expPath);

        // Expense Dots
        for (Point p : expPoints) {
            g2.setColor(ThemeManager.getCardBackgroundColor());
            g2.fillOval(p.x - 5, p.y - 5, 10, 10);
            g2.setColor(new Color(139, 92, 246));
            g2.drawOval(p.x - 5, p.y - 5, 10, 10);
            g2.fillOval(p.x - 3, p.y - 3, 6, 6);
        }

        // Draw Savings Line
        GeneralPath savPath = new GeneralPath();
        savPath.moveTo(savPoints[0].x, savPoints[0].y);
        for (int i = 1; i < count; i++) {
            savPath.lineTo(savPoints[i].x, savPoints[i].y);
        }
        g2.setColor(new Color(6, 182, 212));
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(savPath);

        for (Point p : savPoints) {
            g2.setColor(new Color(6, 182, 212));
            g2.fillOval(p.x - 4, p.y - 4, 8, 8);
        }

        g2.dispose();
    }
}


package com.finance.tracker.ui.charts;

import com.finance.tracker.service.FinanceService.MonthlyReportItem;
import com.finance.tracker.ui.ThemeManager;
import com.finance.tracker.ui.components.RoundedPanel;
import com.finance.tracker.util.CurrencyFormatter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BarChartPanel extends RoundedPanel {
    private List<MonthlyReportItem> data = new ArrayList<>();
    private String hoveredInfo = null;
    private Point mousePoint = new Point(-1, -1);

    public BarChartPanel() {
        super(16);
        setPreferredSize(new Dimension(500, 260));

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePoint = e.getPoint();
                repaint();
            }
        });

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

        // Title and Legend
        g2.setFont(ThemeManager.getFontBold(14f));
        g2.setColor(ThemeManager.getTextPrimary());
        g2.drawString("Income vs. Expenses", 20, 26);

        // Legend: Income (Green) and Expense (Red)
        int legendX = width - 210;
        g2.setColor(ThemeManager.getIncomeColor());
        g2.fillRoundRect(legendX, 15, 12, 12, 4, 4);
        g2.setFont(ThemeManager.getFontMedium(12f));
        g2.setColor(ThemeManager.getTextSecondary());
        g2.drawString("Income", legendX + 16, 26);

        g2.setColor(ThemeManager.getExpenseColor());
        g2.fillRoundRect(legendX + 90, 15, 12, 12, 4, 4);
        g2.setColor(ThemeManager.getTextSecondary());
        g2.drawString("Expense", legendX + 106, 26);

        if (data.isEmpty()) {
            g2.setColor(ThemeManager.getTextMuted());
            g2.setFont(ThemeManager.getFontRegular(13f));
            g2.drawString("No financial transaction data available for the period.", 30, height / 2);
            g2.dispose();
            return;
        }

        int paddingLeft = 50;
        int paddingRight = 30;
        int paddingTop = 50;
        int paddingBottom = 40;

        int chartWidth = width - paddingLeft - paddingRight;
        int chartHeight = height - paddingTop - paddingBottom;

        // Calculate max value for Y-axis scale
        BigDecimal maxVal = BigDecimal.ONE;
        for (MonthlyReportItem item : data) {
            if (item.getIncome().compareTo(maxVal) > 0) maxVal = item.getIncome();
            if (item.getExpenses().compareTo(maxVal) > 0) maxVal = item.getExpenses();
        }
        double maxDouble = maxVal.doubleValue() * 1.15; // 15% headroom

        // Draw horizontal grid lines
        g2.setColor(ThemeManager.getCardBorderColor());
        g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4}, 0));
        g2.setFont(ThemeManager.getFontRegular(10f));

        int gridLines = 4;
        for (int i = 0; i <= gridLines; i++) {
            int y = paddingTop + chartHeight - (int) ((double) i / gridLines * chartHeight);
            g2.drawLine(paddingLeft, y, width - paddingRight, y);

            double lineVal = (maxDouble / gridLines) * i;
            String valStr = String.format("%.0fk", lineVal / 1000);
            g2.setColor(ThemeManager.getTextMuted());
            g2.drawString(valStr, 15, y + 4);
            g2.setColor(ThemeManager.getCardBorderColor());
        }

        // Draw Bars
        int itemCount = data.size();
        int slotWidth = chartWidth / itemCount;
        int barWidth = Math.max(12, Math.min(28, (slotWidth - 24) / 2));

        hoveredInfo = null;
        int tooltipX = 0, tooltipY = 0;

        for (int i = 0; i < itemCount; i++) {
            MonthlyReportItem item = data.get(i);
            int centerX = paddingLeft + (i * slotWidth) + (slotWidth / 2);

            // Income Bar (Left)
            int incBarX = centerX - barWidth - 3;
            double incVal = item.getIncome().doubleValue();
            int incBarHeight = (int) ((incVal / maxDouble) * chartHeight);
            int incBarY = paddingTop + chartHeight - incBarHeight;

            g2.setColor(ThemeManager.getIncomeColor());
            g2.fillRoundRect(incBarX, incBarY, barWidth, incBarHeight, 6, 6);

            // Expense Bar (Right)
            int expBarX = centerX + 3;
            double expVal = item.getExpenses().doubleValue();
            int expBarHeight = (int) ((expVal / maxDouble) * chartHeight);
            int expBarY = paddingTop + chartHeight - expBarHeight;

            g2.setColor(ThemeManager.getExpenseColor());
            g2.fillRoundRect(expBarX, expBarY, barWidth, expBarHeight, 6, 6);

            // Month Label
            g2.setColor(ThemeManager.getTextSecondary());
            g2.setFont(ThemeManager.getFontMedium(11f));
            FontMetrics fm = g2.getFontMetrics();
            int labelWidth = fm.stringWidth(item.getMonthLabel());
            g2.drawString(item.getMonthLabel(), centerX - (labelWidth / 2), height - 15);

            // Hover Hit Detection
            if (mousePoint.x >= incBarX && mousePoint.x <= incBarX + barWidth &&
                mousePoint.y >= incBarY && mousePoint.y <= paddingTop + chartHeight) {
                hoveredInfo = item.getMonthLabel() + " Income: " + CurrencyFormatter.format(item.getIncome());
                tooltipX = mousePoint.x;
                tooltipY = incBarY - 10;
            } else if (mousePoint.x >= expBarX && mousePoint.x <= expBarX + barWidth &&
                       mousePoint.y >= expBarY && mousePoint.y <= paddingTop + chartHeight) {
                hoveredInfo = item.getMonthLabel() + " Expense: " + CurrencyFormatter.format(item.getExpenses());
                tooltipX = mousePoint.x;
                tooltipY = expBarY - 10;
            }
        }

        // Draw Tooltip if hovered
        if (hoveredInfo != null) {
            g2.setFont(ThemeManager.getFontMedium(12f));
            FontMetrics tfm = g2.getFontMetrics();
            int tw = tfm.stringWidth(hoveredInfo) + 16;
            int th = 26;
            int tx = Math.max(10, Math.min(width - tw - 10, tooltipX - (tw / 2)));
            int ty = Math.max(35, tooltipY - th);

            g2.setColor(new Color(15, 23, 42, 230));
            g2.fillRoundRect(tx, ty, tw, th, 8, 8);
            g2.setColor(Color.WHITE);
            g2.drawString(hoveredInfo, tx + 8, ty + 17);
        }

        g2.dispose();
    }
}


package com.finance.tracker.ui.charts;

import com.finance.tracker.ui.ThemeManager;
import com.finance.tracker.ui.components.RoundedPanel;
import com.finance.tracker.util.CurrencyFormatter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

public class DoughnutChartPanel extends RoundedPanel {
    private Map<String, BigDecimal> categoryData = new LinkedHashMap<>();
    private final Color[] PALETTE = {
        new Color(59, 130, 246),  // Blue
        new Color(16, 185, 129),  // Emerald
        new Color(245, 158, 11),  // Amber
        new Color(168, 85, 247),  // Purple
        new Color(239, 68, 68),   // Red
        new Color(6, 182, 212),   // Cyan
        new Color(249, 115, 22),  // Orange
        new Color(236, 72, 153),  // Pink
        new Color(20, 184, 166),  // Teal
        new Color(100, 116, 139)  // Slate
    };

    private int hoveredIndex = -1;
    private Point mousePoint = new Point(-1, -1);

    public DoughnutChartPanel() {
        super(16);
        setPreferredSize(new Dimension(460, 260));

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePoint = e.getPoint();
                repaint();
            }
        });

        ThemeManager.addThemeListener(this::repaint);
    }

    public void setData(Map<String, BigDecimal> data) {
        this.categoryData = data != null ? data : new LinkedHashMap<>();
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
        g2.drawString("Expense Breakdown by Category", 20, 26);

        if (categoryData.isEmpty()) {
            g2.setColor(ThemeManager.getTextMuted());
            g2.setFont(ThemeManager.getFontRegular(13f));
            g2.drawString("No expense records found for this period.", 30, height / 2);
            g2.dispose();
            return;
        }

        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal val : categoryData.values()) {
            total = total.add(val);
        }

        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            g2.setColor(ThemeManager.getTextMuted());
            g2.drawString("Total expenses are zero.", 30, height / 2);
            g2.dispose();
            return;
        }

        int diameter = Math.min(160, height - 70);
        int chartX = 25;
        int chartY = 45;
        int innerDiameter = diameter - 50;
        int innerX = chartX + (diameter - innerDiameter) / 2;
        int innerY = chartY + (diameter - innerDiameter) / 2;

        double currentAngle = 90.0;
        List<Map.Entry<String, BigDecimal>> entries = new ArrayList<>(categoryData.entrySet());
        hoveredIndex = -1;

        // Draw doughnut slices
        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<String, BigDecimal> entry = entries.get(i);
            double slicePercentage = entry.getValue().doubleValue() / total.doubleValue();
            double arcAngle = slicePercentage * 360.0;
            Color sliceColor = PALETTE[i % PALETTE.length];

            // Build ring slice shape
            Arc2D.Double outerArc = new Arc2D.Double(chartX, chartY, diameter, diameter, currentAngle, -arcAngle, Arc2D.PIE);
            Area sliceArea = new Area(outerArc);
            Ellipse2D.Double innerHole = new Ellipse2D.Double(innerX, innerY, innerDiameter, innerDiameter);
            sliceArea.subtract(new Area(innerHole));

            if (sliceArea.contains(mousePoint)) {
                hoveredIndex = i;
                g2.setColor(sliceColor.brighter());
            } else {
                g2.setColor(sliceColor);
            }

            g2.fill(sliceArea);
            currentAngle -= arcAngle;
        }

        // Center Text: Total
        g2.setColor(ThemeManager.getTextSecondary());
        g2.setFont(ThemeManager.getFontRegular(10f));
        FontMetrics fm = g2.getFontMetrics();
        String totalLabel = "Total";
        g2.drawString(totalLabel, chartX + (diameter - fm.stringWidth(totalLabel)) / 2, chartY + (diameter / 2) - 4);

        g2.setFont(ThemeManager.getFontBold(11f));
        g2.setColor(ThemeManager.getTextPrimary());
        FontMetrics bfm = g2.getFontMetrics();
        String totalFormatted = CurrencyFormatter.format(total);
        g2.drawString(totalFormatted, chartX + (diameter - bfm.stringWidth(totalFormatted)) / 2, chartY + (diameter / 2) + 12);

        // Draw Legend List on the right
        int legendX = chartX + diameter + 30;
        int legendY = 50;
        int maxLegendItems = Math.min(6, entries.size());

        for (int i = 0; i < maxLegendItems; i++) {
            Map.Entry<String, BigDecimal> entry = entries.get(i);
            Color itemColor = PALETTE[i % PALETTE.length];
            double pct = (entry.getValue().doubleValue() / total.doubleValue()) * 100.0;

            // Color dot
            g2.setColor(itemColor);
            g2.fillRoundRect(legendX, legendY - 8, 10, 10, 4, 4);

            // Category name & percentage
            g2.setColor(i == hoveredIndex ? ThemeManager.getPrimaryAccent() : ThemeManager.getTextPrimary());
            g2.setFont(ThemeManager.getFontMedium(11f));
            String catName = entry.getKey().length() > 16 ? entry.getKey().substring(0, 14) + ".." : entry.getKey();
            g2.drawString(catName, legendX + 16, legendY);

            // Amount and percent
            g2.setColor(ThemeManager.getTextSecondary());
            g2.setFont(ThemeManager.getFontRegular(10f));
            String amountStr = String.format("%s (%.1f%%)", CurrencyFormatter.format(entry.getValue()), pct);
            g2.drawString(amountStr, legendX + 16, legendY + 12);

            legendY += 28;
        }

        if (entries.size() > 6) {
            g2.setColor(ThemeManager.getTextMuted());
            g2.setFont(ThemeManager.getFontRegular(10f));
            g2.drawString("+ " + (entries.size() - 6) + " more categories", legendX + 16, legendY);
        }

        g2.dispose();
    }
}


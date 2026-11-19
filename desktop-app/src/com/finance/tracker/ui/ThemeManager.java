package com.finance.tracker.ui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ThemeManager {
    public enum Theme { LIGHT, DARK }

    private static Theme currentTheme = Theme.LIGHT;
    private static final List<Runnable> listeners = new ArrayList<>();

    // Palette Definitions
    public static Color getBackgroundColor() {
        return currentTheme == Theme.LIGHT ? new Color(248, 250, 252) : new Color(15, 23, 42);
    }

    public static Color getSidebarColor() {
        return currentTheme == Theme.LIGHT ? new Color(255, 255, 255) : new Color(30, 41, 59);
    }

    public static Color getCardBackgroundColor() {
        return currentTheme == Theme.LIGHT ? new Color(255, 255, 255) : new Color(30, 41, 59);
    }

    public static Color getCardBorderColor() {
        return currentTheme == Theme.LIGHT ? new Color(226, 232, 240) : new Color(51, 65, 85);
    }

    public static Color getTextPrimary() {
        return currentTheme == Theme.LIGHT ? new Color(15, 23, 42) : new Color(248, 250, 252);
    }

    public static Color getTextSecondary() {
        return currentTheme == Theme.LIGHT ? new Color(100, 116, 139) : new Color(148, 163, 184);
    }

    public static Color getTextMuted() {
        return currentTheme == Theme.LIGHT ? new Color(148, 163, 184) : new Color(100, 116, 139);
    }

    public static Color getPrimaryAccent() {
        return currentTheme == Theme.LIGHT ? new Color(37, 99, 235) : new Color(59, 130, 246);
    }

    public static Color getIncomeColor() {
        return currentTheme == Theme.LIGHT ? new Color(16, 185, 129) : new Color(52, 211, 153);
    }

    public static Color getIncomeLightBg() {
        return currentTheme == Theme.LIGHT ? new Color(236, 253, 245) : new Color(6, 78, 59);
    }

    public static Color getExpenseColor() {
        return currentTheme == Theme.LIGHT ? new Color(239, 68, 68) : new Color(248, 113, 113);
    }

    public static Color getExpenseLightBg() {
        return currentTheme == Theme.LIGHT ? new Color(254, 242, 242) : new Color(127, 29, 29);
    }

    public static Color getWarningColor() {
        return currentTheme == Theme.LIGHT ? new Color(245, 158, 11) : new Color(251, 191, 36);
    }

    public static Color getTableAlternateRow() {
        return currentTheme == Theme.LIGHT ? new Color(248, 250, 252) : new Color(30, 41, 59, 150);
    }

    public static Color getTableHeaderBg() {
        return currentTheme == Theme.LIGHT ? new Color(241, 245, 249) : new Color(15, 23, 42);
    }

    public static Color getInputBackground() {
        return currentTheme == Theme.LIGHT ? new Color(255, 255, 255) : new Color(15, 23, 42);
    }

    public static Font getFontRegular(float size) {
        return new Font("Segoe UI", Font.PLAIN, (int) size);
    }

    public static Font getFontMedium(float size) {
        return new Font("Segoe UI", Font.BOLD, (int) size);
    }

    public static Font getFontBold(float size) {
        return new Font("Segoe UI", Font.BOLD, (int) size);
    }

    public static Theme getCurrentTheme() {
        return currentTheme;
    }

    public static void toggleTheme() {
        currentTheme = (currentTheme == Theme.LIGHT) ? Theme.DARK : Theme.LIGHT;
        notifyListeners();
    }

    public static void setTheme(Theme theme) {
        currentTheme = theme;
        notifyListeners();
    }

    public static void addThemeListener(Runnable r) {
        listeners.add(r);
    }

    private static void notifyListeners() {
        for (Runnable r : listeners) {
            try {
                r.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}


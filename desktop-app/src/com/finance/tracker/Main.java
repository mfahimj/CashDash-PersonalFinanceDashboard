package com.finance.tracker;

import com.finance.tracker.ui.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // High DPI & Anti-aliasing properties
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                // Set system Look & Feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}


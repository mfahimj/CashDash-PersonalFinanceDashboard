package com.finance.tracker.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtils {
    public static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    public static final DateTimeFormatter FULL_HEADER_FORMAT = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");

    private static String activePattern = "yyyy-MM-dd";

    public static void setActivePattern(String pattern) {
        activePattern = pattern;
    }

    public static String format(LocalDate date) {
        if (date == null) return "";
        try {
            return date.format(DateTimeFormatter.ofPattern(activePattern));
        } catch (Exception e) {
            return date.format(ISO_FORMAT);
        }
    }

    public static String formatDisplay(LocalDate date) {
        if (date == null) return "";
        return date.format(DISPLAY_FORMAT);
    }

    public static String formatFullHeader(LocalDate date) {
        if (date == null) return "";
        return date.format(FULL_HEADER_FORMAT);
    }

    public static LocalDate parse(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        String trimmed = text.trim();
        try {
            return LocalDate.parse(trimmed, DateTimeFormatter.ofPattern(activePattern));
        } catch (DateTimeParseException e1) {
            try {
                return LocalDate.parse(trimmed, ISO_FORMAT);
            } catch (DateTimeParseException e2) {
                try {
                    return LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                } catch (Exception e3) {
                    return null;
                }
            }
        }
    }

    public static String getMonthName(int month) {
        if (month < 1 || month > 12) return "";
        return java.time.Month.of(month).name();
    }
}


package com.app.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Format date/time vi-VN: dd/MM/yyyy HH:mm. */
public final class DateFormatter {

    public static final DateTimeFormatter DATE     = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter TIME     = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter SQL      = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateFormatter() {}

    public static String formatDate(LocalDate d) { return d == null ? "" : DATE.format(d); }
    public static String formatTime(LocalTime t) { return t == null ? "" : TIME.format(t); }
    public static String formatDateTime(LocalDateTime dt) { return dt == null ? "" : DATETIME.format(dt); }
    public static String toSql(LocalDateTime dt) { return dt == null ? "" : SQL.format(dt); }

    /** Parse SQL TEXT format (yyyy-MM-dd HH:mm:ss) → LocalDateTime, null nếu invalid */
    public static LocalDateTime fromSql(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDateTime.parse(s, SQL);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}

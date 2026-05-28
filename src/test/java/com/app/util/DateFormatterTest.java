package com.app.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class DateFormatterTest {

    @Test
    void formatDate() {
        LocalDate d = LocalDate.of(2026, 5, 26);
        assertEquals("26/05/2026", DateFormatter.formatDate(d));
    }

    @Test
    void formatTime() {
        LocalTime t = LocalTime.of(10, 55);
        assertEquals("10:55", DateFormatter.formatTime(t));
    }

    @Test
    void formatDateTime() {
        LocalDateTime dt = LocalDateTime.of(2026, 5, 26, 10, 55);
        assertEquals("26/05/2026 10:55", DateFormatter.formatDateTime(dt));
    }

    @Test
    void toSqlVaFromSqlRoundtrip() {
        LocalDateTime dt = LocalDateTime.of(2026, 5, 26, 10, 55, 30);
        String sql = DateFormatter.toSql(dt);
        assertEquals("2026-05-26 10:55:30", sql);
        assertEquals(dt, DateFormatter.fromSql(sql));
    }

    @Test
    void fromSqlNullKhiKhongHopLe() {
        assertNull(DateFormatter.fromSql("not a date"));
        assertNull(DateFormatter.fromSql(""));
        assertNull(DateFormatter.fromSql(null));
    }

    @Test
    void formatNullTraVeChuoiTrong() {
        assertEquals("", DateFormatter.formatDate(null));
        assertEquals("", DateFormatter.formatTime(null));
        assertEquals("", DateFormatter.formatDateTime(null));
    }
}

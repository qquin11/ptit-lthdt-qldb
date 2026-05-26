package com.app.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyFormatterTest {

    @Test
    void formatVndCoDauChamPhanCach() {
        // 145000 phải hiển thị "145.000 đ"
        assertEquals("145.000 đ", CurrencyFormatter.format(145000));
    }

    @Test
    void formatPlainKhongCoKyTuTienTe() {
        assertEquals("12.500.000", CurrencyFormatter.formatPlain(12_500_000));
    }

    @Test
    void formatSoAm() {
        assertEquals("-50.000 đ", CurrencyFormatter.format(-50000));
    }

    @Test
    void formatZero() {
        assertEquals("0 đ", CurrencyFormatter.format(0));
    }

    @Test
    void parseChuoiCoVnd() {
        assertEquals(145000.0, CurrencyFormatter.parse("145.000 đ"));
    }

    @Test
    void parseChuoiTrong() {
        assertEquals(0.0, CurrencyFormatter.parse(""));
    }

    @Test
    void parseChuoiKhongHopLeNemException() {
        assertThrows(NumberFormatException.class, () -> CurrencyFormatter.parse("abc"));
    }
}

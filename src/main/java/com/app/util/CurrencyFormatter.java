package com.app.util;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Format VND theo locale vi-VN. Dùng xuyên suốt app cho mọi số tiền.
 *
 * <p>Examples:
 * <ul>
 *   <li>{@code format(145000)} → "145.000 đ"</li>
 *   <li>{@code formatPlain(145000)} → "145.000"</li>
 *   <li>{@code parse("145.000 đ")} → 145000</li>
 * </ul>
 */
public final class CurrencyFormatter {

    private static final Locale VN = Locale.forLanguageTag("vi-VN");
    private static final NumberFormat NUMBER = NumberFormat.getNumberInstance(VN);

    private CurrencyFormatter() {}

    /** "145.000 đ" */
    public static String format(double amount) {
        return NUMBER.format((long) amount) + " đ";
    }

    /** "145.000" (no currency symbol) */
    public static String formatPlain(double amount) {
        return NUMBER.format((long) amount);
    }

    /** Parse "145.000 đ" hoặc "145000" → 145000.0. Throws NumberFormatException nếu sai. */
    public static double parse(String text) {
        if (text == null || text.isBlank()) return 0;
        String clean = text.replace(".", "")
                .replace(",", "")
                .replace("đ", "")
                .replace("VND", "")
                .trim();
        return Double.parseDouble(clean);
    }
}

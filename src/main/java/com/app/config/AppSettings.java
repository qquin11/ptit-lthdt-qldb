package com.app.config;

import java.util.prefs.Preferences;

/**
 * Cài đặt người dùng lưu qua {@link Preferences} (registry Windows / plist macOS).
 *
 * <p>Gom các tham số chỉnh được trong màn Cài đặt: thông tin nhà hàng (in trên
 * hóa đơn) và thuế VAT mặc định. Tất cả đều có default fallback nếu chưa cấu hình.
 */
public final class AppSettings {

    private static final Preferences PREFS = Preferences.userNodeForPackage(AppSettings.class);

    private static final String K_REST_NAME  = "restaurant_name";
    private static final String K_REST_ADDR  = "restaurant_address";
    private static final String K_REST_PHONE = "restaurant_phone";
    private static final String K_VAT_RATE   = "vat_rate";

    private static final String DEFAULT_NAME  = "NHÀ HÀNG ABC";
    private static final String DEFAULT_ADDR  = "123 Trần Phú, Hà Nội";
    private static final String DEFAULT_PHONE = "0901-234-567";

    private AppSettings() {}

    // RESTAURANT INFO

    public static String restaurantName()    { return PREFS.get(K_REST_NAME, DEFAULT_NAME); }
    public static String restaurantAddress() { return PREFS.get(K_REST_ADDR, DEFAULT_ADDR); }
    public static String restaurantPhone()   { return PREFS.get(K_REST_PHONE, DEFAULT_PHONE); }

    public static void setRestaurantInfo(String name, String address, String phone) {
        PREFS.put(K_REST_NAME, safe(name, DEFAULT_NAME));
        PREFS.put(K_REST_ADDR, safe(address, DEFAULT_ADDR));
        PREFS.put(K_REST_PHONE, safe(phone, DEFAULT_PHONE));
    }

    // VAT

    /** VAT rate dạng thập phân (0.10 = 10%). Fallback {@link AppConfig#DEFAULT_VAT_RATE}. */
    public static double vatRate() {
        return PREFS.getDouble(K_VAT_RATE, AppConfig.DEFAULT_VAT_RATE);
    }

    /** Set VAT theo phần trăm (10 → 0.10). Clamp 0..100. */
    public static void setVatPercent(double percent) {
        double p = Math.max(0, Math.min(100, percent));
        PREFS.putDouble(K_VAT_RATE, p / 100.0);
    }

    private static String safe(String v, String fallback) {
        return (v == null || v.isBlank()) ? fallback : v.trim();
    }
}

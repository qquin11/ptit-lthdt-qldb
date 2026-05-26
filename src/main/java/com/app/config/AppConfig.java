package com.app.config;

/**
 * App-wide constants — version, paths, window sizes, preference keys.
 * Tách riêng để dễ tìm + update 1 chỗ.
 */
public final class AppConfig {

    private AppConfig() {}

    // ===================== APP METADATA =====================

    public static final String APP_NAME    = "ResMan POS";
    public static final String APP_BRAND   = "Restaurant Manager";
    public static final String APP_VERSION = "1.0-SNAPSHOT";
    public static final String APP_VENDOR  = "PTIT LTHDT";

    // ===================== PATHS =====================

    public static final String DB_DIR    = "./data";
    public static final String DB_FILE   = "nhahang.db";
    public static final String DB_PATH   = DB_DIR + "/" + DB_FILE;
    public static final String IMAGE_DIR = "./images";

    // ===================== WINDOW SIZES =====================

    public static final int MAIN_WIDTH      = 1280;
    public static final int MAIN_HEIGHT     = 800;
    public static final int MAIN_MIN_WIDTH  = 1024;
    public static final int MAIN_MIN_HEIGHT = 640;

    public static final int LOGIN_WIDTH  = 400;
    public static final int LOGIN_HEIGHT = 500;

    public static final int INVOICE_WIDTH  = 380;
    public static final int INVOICE_HEIGHT = 600;

    // ===================== PREFERENCES KEYS =====================

    /** java.util.prefs key cho theme (LIGHT/DARK) */
    public static final String PREF_THEME = "theme";
    /** java.util.prefs key cho username remember-me */
    public static final String PREF_USERNAME = "remembered_username";
    /** java.util.prefs key cho sidebar collapsed state */
    public static final String PREF_SIDEBAR_COLLAPSED = "sidebar_collapsed";

    // ===================== BUSINESS RULES =====================

    /** VAT mặc định cho hóa đơn (10%) — match schema hoa_don.vat DEFAULT 0.1 */
    public static final double DEFAULT_VAT_RATE = 0.10;

    /** Số phút khoảng đặt bàn tránh conflict (2 giờ) */
    public static final int RESERVATION_SLOT_HOURS = 2;

    /** Max failed login trước khi lock */
    public static final int LOGIN_MAX_ATTEMPTS = 3;
    /** Lock duration (giây) */
    public static final int LOGIN_LOCK_SECONDS = 30;
}

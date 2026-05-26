package com.app.config;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;

/**
 * Typography tokens — fonts sized theo design system.
 *
 * <p>Gọi {@link #init()} một lần khi app start (sau khi LaF được set, trước khi
 * tạo Frame). Sau init, các field public {@link #display}, {@link #h1}, ... có sẵn.
 *
 * <p><b>Inter font (YAGNI):</b> Phase 02 KHÔNG bundle Inter TTFs để giữ jar nhẹ.
 * Fallback dùng "Segoe UI" trên Windows / "SansSerif" cross-platform.
 * Phase 12 polish có thể bundle Inter TTFs nếu cần upgrade typography.
 */
public final class AppFonts {

    /** Font family ưu tiên — chưa bundle, sẽ fallback */
    private static final String PREFERRED_FAMILY = "Inter";
    /** Fallback Windows */
    private static final String FALLBACK_FAMILY = "Segoe UI";

    public static Font display;
    public static Font h1;
    public static Font h2;
    public static Font body;
    public static Font small;
    public static Font mono;
    public static Font icon;       // emoji + symbol (Segoe UI Emoji có sẵn Windows)
    public static Font iconLarge;

    private AppFonts() {}

    /**
     * Khởi tạo bộ font. Gọi sau {@code ThemeManager.init()}.
     * Ưu tiên Inter nếu có TTF trong classpath; nếu không, dùng Segoe UI / SansSerif.
     */
    public static void init() {
        String family = resolveFamily();
        display = new Font(family, Font.BOLD,  24);
        h1      = new Font(family, Font.BOLD,  20);
        h2      = new Font(family, Font.BOLD,  16);
        body    = new Font(family, Font.PLAIN, 14);
        small   = new Font(family, Font.PLAIN, 12);
        mono    = new Font(Font.MONOSPACED, Font.PLAIN, 13);
        icon       = new Font("Segoe UI Emoji", Font.PLAIN, 16);
        iconLarge  = new Font("Segoe UI Emoji", Font.PLAIN, 28);
    }

    /**
     * Try register Inter font (nếu TTF có trong classpath); fallback to system family.
     * Phase 12 polish có thể bundle Inter TTFs nếu cần upgrade typography.
     */
    private static String resolveFamily() {
        if (tryRegisterInter()) {
            return PREFERRED_FAMILY;
        }
        // Fallback theo platform — Segoe UI mặc định Windows 10+
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (String name : ge.getAvailableFontFamilyNames()) {
            if (FALLBACK_FAMILY.equalsIgnoreCase(name)) {
                return FALLBACK_FAMILY;
            }
        }
        return Font.SANS_SERIF;
    }

    /** Đọc fonts/Inter-Regular.ttf từ classpath; trả false nếu không có. */
    private static boolean tryRegisterInter() {
        try (InputStream in = AppFonts.class.getResourceAsStream("/fonts/Inter-Regular.ttf")) {
            if (in == null) return false;
            Font inter = Font.createFont(Font.TRUETYPE_FONT, in);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(inter);
            return true;
        } catch (IOException | FontFormatException e) {
            return false;
        }
    }
}

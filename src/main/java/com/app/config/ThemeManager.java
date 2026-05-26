package com.app.config;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.UIManager;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

/**
 * Quản lý theme Light/Dark cho toàn app.
 *
 * <p>Workflow:
 * <ol>
 *   <li>{@link #init()} 1 lần trong {@code Main.main()} (trước khi tạo Frame)</li>
 *   <li>{@link #toggle()} từ button trên TopBar khi user click theme icon</li>
 *   <li>Components muốn react khi đổi theme: {@link #addThemeChangeListener}</li>
 * </ol>
 *
 * <p>Theme persist qua {@link Preferences} (registry Windows / plist macOS).
 * FlatLaf tự load <code>FlatLightLaf.properties</code> + <code>FlatDarkLaf.properties</code>
 * từ classpath root để override accent #2563EB, radius 8px, focus ring.
 */
public final class ThemeManager {

    public enum Theme { LIGHT, DARK }

    private static Theme current = Theme.LIGHT;
    private static final List<Consumer<Theme>> listeners = new ArrayList<>();
    private static final Preferences PREFS = Preferences.userNodeForPackage(ThemeManager.class);

    private ThemeManager() {}

    /**
     * Khởi tạo theme từ saved preference, apply LaF + register fonts.
     * Gọi 1 lần trong {@code Main.main()} TRƯỚC khi tạo Frame đầu tiên.
     */
    public static void init() {
        String saved = PREFS.get(AppConfig.PREF_THEME, Theme.LIGHT.name());
        Theme initial;
        try {
            initial = Theme.valueOf(saved);
        } catch (IllegalArgumentException e) {
            initial = Theme.LIGHT;
        }
        applyInternal(initial, /* notify */ false);
        AppFonts.init();
    }

    /** Theme hiện tại */
    public static Theme current() {
        return current;
    }

    /** Set theme + persist + notify listeners + update mọi window đang mở */
    public static void apply(Theme theme) {
        if (theme == null || theme == current) return;
        applyInternal(theme, /* notify */ true);
    }

    /** Light ↔ Dark */
    public static void toggle() {
        apply(current == Theme.LIGHT ? Theme.DARK : Theme.LIGHT);
    }

    /** Component đăng ký để repaint custom khi theme đổi */
    public static void addThemeChangeListener(Consumer<Theme> listener) {
        if (listener != null) listeners.add(listener);
    }

    public static void removeThemeChangeListener(Consumer<Theme> listener) {
        listeners.remove(listener);
    }

    // ===================== INTERNAL =====================

    private static void applyInternal(Theme theme, boolean notify) {
        try {
            if (theme == Theme.LIGHT) {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } else {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            }
            current = theme;
            PREFS.put(AppConfig.PREF_THEME, theme.name());

            // Update mọi window đã mở (FlatLaf utility)
            FlatLaf.updateUI();

            if (notify) {
                for (Consumer<Theme> l : new ArrayList<>(listeners)) {
                    try {
                        l.accept(theme);
                    } catch (RuntimeException ex) {
                        // listener riêng lỗi không được ảnh hưởng những listener khác
                        System.err.println("Theme listener error: " + ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Theme apply failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

package com.app.util;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.Icon;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache SVG icons từ classpath `/icons/`. Gracefully trả null nếu không có icon.
 *
 * <p>Phase 03 chưa bundle SVG nào — caller dùng emoji fallback hoặc null check.
 * Drop SVG files vào {@code src/main/resources/icons/} sẽ tự pick up.
 */
public final class IconLoader {

    private static final Map<String, FlatSVGIcon> CACHE = new HashMap<>();

    private IconLoader() {}

    /** Load icon kích thước square. Trả null nếu file không tồn tại trong classpath. */
    public static Icon load(String name, int size) {
        String key = name + "@" + size;
        FlatSVGIcon cached = CACHE.get(key);
        if (cached != null) return cached;
        try {
            FlatSVGIcon icon = new FlatSVGIcon("icons/" + name + ".svg", size, size);
            if (icon.hasFound()) {
                CACHE.put(key, icon);
                return icon;
            }
        } catch (RuntimeException ignored) {}
        return null;
    }

    /** Load icon với màu tint (override fill color). */
    public static Icon load(String name, int size, Color tint) {
        Icon base = load(name, size);
        if (base instanceof FlatSVGIcon svg && tint != null) {
            return svg.derive(color -> tint);
        }
        return base;
    }
}

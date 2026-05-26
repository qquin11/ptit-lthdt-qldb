package com.app.config;

import java.awt.Color;

/**
 * Design tokens — bảng màu chuẩn cho toàn bộ ứng dụng.
 *
 * <p>Tổ chức:
 * <ul>
 *   <li>Status colors: dùng chung cả Light/Dark theme (ổn định visual cue)</li>
 *   <li>Table status: alias màu trạng thái bàn (ghép semantic name)</li>
 *   <li>LIGHT_* / DARK_*: tokens riêng cho từng theme</li>
 * </ul>
 *
 * <p>Lưu ý: FlatLaf đã cấp tokens runtime qua UIManager (Component.background,
 * Label.foreground, ...). Class này KHÔNG thay thế FlatLaf tokens — chỉ bổ sung
 * cho status badges, table status, custom paint nơi UIManager không cover.
 */
public final class AppColors {

    private AppColors() {}

    // STATUS COLORS (shared)

    public static final Color SUCCESS = Color.decode("#10B981"); // green
    public static final Color WARNING = Color.decode("#F59E0B"); // amber
    public static final Color INFO    = Color.decode("#6366F1"); // indigo
    public static final Color DANGER  = Color.decode("#EF4444"); // red
    public static final Color NEUTRAL = Color.decode("#94A3B8"); // slate

    // TABLE STATUS (alias)

    /** Bàn trống — hiển thị xanh lá */
    public static final Color TABLE_EMPTY    = SUCCESS;
    /** Bàn đang sử dụng — hiển thị amber */
    public static final Color TABLE_OCCUPIED = WARNING;
    /** Bàn đặt trước — hiển thị indigo */
    public static final Color TABLE_RESERVED = INFO;
    /** Bàn bảo trì/khóa — hiển thị xám */
    public static final Color TABLE_LOCKED   = NEUTRAL;

    // LIGHT THEME TOKENS

    public static final Color LIGHT_BG           = Color.decode("#FFFFFF");
    public static final Color LIGHT_SURFACE      = Color.decode("#F8FAFC");
    public static final Color LIGHT_SURFACE_ALT  = Color.decode("#F1F5F9");
    public static final Color LIGHT_BORDER       = Color.decode("#E2E8F0");
    public static final Color LIGHT_TEXT         = Color.decode("#0F172A");
    public static final Color LIGHT_TEXT_MUTED   = Color.decode("#64748B");
    public static final Color LIGHT_ACCENT       = Color.decode("#2563EB");
    public static final Color LIGHT_ACCENT_HOVER = Color.decode("#1D4ED8");

    // DARK THEME TOKENS

    public static final Color DARK_BG           = Color.decode("#0F172A");
    public static final Color DARK_SURFACE      = Color.decode("#1E293B");
    public static final Color DARK_SURFACE_ALT  = Color.decode("#334155");
    public static final Color DARK_BORDER       = Color.decode("#475569");
    public static final Color DARK_TEXT         = Color.decode("#F1F5F9");
    public static final Color DARK_TEXT_MUTED   = Color.decode("#94A3B8");
    public static final Color DARK_ACCENT       = Color.decode("#3B82F6");
    public static final Color DARK_ACCENT_HOVER = Color.decode("#60A5FA");
}

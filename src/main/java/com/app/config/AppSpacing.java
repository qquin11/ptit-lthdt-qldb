package com.app.config;

/**
 * Spacing & radius tokens — grid 8px chuẩn cho mọi padding/margin/gap.
 *
 * <p>Cách dùng trong MigLayout:
 * <pre>{@code
 *   new MigLayout("insets " + AppSpacing.LG + ", gap " + AppSpacing.MD)
 * }</pre>
 *
 * <p>Trong JPanel padding:
 * <pre>{@code
 *   panel.setBorder(BorderFactory.createEmptyBorder(
 *       AppSpacing.LG, AppSpacing.LG, AppSpacing.LG, AppSpacing.LG));
 * }</pre>
 */
public final class AppSpacing {

    private AppSpacing() {}

    // ===================== SPACING (8px grid) =====================

    public static final int XS   = 4;
    public static final int SM   = 8;
    public static final int MD   = 12;
    public static final int LG   = 16;
    public static final int XL   = 24;
    public static final int XXL  = 32;
    public static final int XXXL = 48;

    // ===================== BORDER RADIUS =====================

    public static final int RADIUS_SM   = 4;
    public static final int RADIUS_MD   = 8;
    public static final int RADIUS_LG   = 12;
    public static final int RADIUS_PILL = 999;

    // ===================== COMPONENT HEIGHTS (UX standards) =====================

    /** Button & input field height chuẩn */
    public static final int H_INPUT  = 36;
    /** Sidebar item height */
    public static final int H_SIDEBAR_ITEM = 44;
    /** TopBar height */
    public static final int H_TOPBAR = 56;
    /** StatusBar height */
    public static final int H_STATUSBAR = 24;
    /** Sidebar widths */
    public static final int W_SIDEBAR_EXPANDED  = 220;
    public static final int W_SIDEBAR_COLLAPSED = 64;
}

package com.app.view.common;

import com.app.config.AppColors;
import com.app.config.AppFonts;
import com.app.config.AppSpacing;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

/**
 * Toast notification — top-right, auto-dismiss 3s, slide-in 200ms.
 * Static API: {@link #success}, {@link #error}, {@link #warning}, {@link #info}.
 * Caller chỉ cần truyền parent component bất kỳ trong app.
 */
public class Toast extends JPanel {

    public enum Type { SUCCESS, WARNING, ERROR, INFO }

    private static final int DEFAULT_DURATION_MS = 3000;

    public Toast(Type type, String message) {
        super(new BorderLayout(AppSpacing.SM, 0));
        setBorder(BorderFactory.createEmptyBorder(
                AppSpacing.MD, AppSpacing.LG, AppSpacing.MD, AppSpacing.LG));
        setBackground(bgColorFor(type));
        putClientProperty("FlatLaf.style", "arc: 8");

        JLabel icon = new JLabel(iconFor(type));
        icon.setForeground(Color.WHITE);
        icon.setFont(AppFonts.icon == null ? icon.getFont() : AppFonts.icon.deriveFont(java.awt.Font.BOLD, 18f));

        // Giới hạn width để message dài tự wrap nhiều dòng, không kéo toast rộng quá rồi bị cắt chữ
        JLabel msg = new JLabel("<html><div style='width:260px'>" + escapeHtml(message) + "</div></html>");
        msg.setForeground(Color.WHITE);
        msg.setFont(AppFonts.body == null ? msg.getFont() : AppFonts.body);

        add(icon, BorderLayout.WEST);
        add(msg, BorderLayout.CENTER);
    }

    // STATIC API

    public static void success(Component parent, String msg) { show(parent, Type.SUCCESS, msg); }
    public static void warning(Component parent, String msg) { show(parent, Type.WARNING, msg); }
    public static void error  (Component parent, String msg) { show(parent, Type.ERROR,   msg); }
    public static void info   (Component parent, String msg) { show(parent, Type.INFO,    msg); }

    public static void show(Component parent, Type type, String msg) {
        ToastManager.show(parent, new Toast(type, msg), DEFAULT_DURATION_MS);
    }

    // HELPERS

    private static Color bgColorFor(Type type) {
        return switch (type) {
            case SUCCESS -> AppColors.SUCCESS;
            case WARNING -> AppColors.WARNING;
            case ERROR   -> AppColors.DANGER;
            case INFO    -> AppColors.INFO;
        };
    }

    private static String iconFor(Type type) {
        return switch (type) {
            case SUCCESS -> "✓";
            case WARNING -> "⚠";
            case ERROR   -> "✕";
            case INFO    -> "ℹ";
        };
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}

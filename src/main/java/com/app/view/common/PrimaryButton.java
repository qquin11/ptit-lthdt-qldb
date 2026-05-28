package com.app.view.common;

import com.app.config.AppFonts;
import com.app.config.AppSpacing;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import java.awt.Dimension;

/**
 * Primary action button — FlatLaf "default" style (accent bg + white text).
 * Dùng cho action chính của form/dialog (Lưu, Thanh toán, Đăng nhập).
 */
public class PrimaryButton extends JButton {

    public PrimaryButton(String text) {
        super(text);
        setFont(AppFonts.body == null ? getFont() : AppFonts.body);
        putClientProperty("JButton.buttonType", "default"); // FlatLaf accent fill
        setBorder(new EmptyBorder(AppSpacing.SM, AppSpacing.LG, AppSpacing.SM, AppSpacing.LG));
        setFocusPainted(false);
    }

    // Ép chiều cao tối thiểu nhưng để width tự tính theo text (tránh cắt chữ thành "...")
    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = Math.max(d.height, AppSpacing.H_INPUT);
        return d;
    }
}

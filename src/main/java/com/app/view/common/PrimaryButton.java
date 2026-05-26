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
        setPreferredSize(new Dimension(getPreferredSize().width, AppSpacing.H_INPUT));
        setFocusPainted(false);
    }
}

package com.app.view.common;

import com.app.config.AppFonts;
import com.app.config.AppSpacing;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import java.awt.Dimension;

/** Ghost button — transparent bg, hover surface alt. Dùng cho action tertiary/icon-only. */
public class GhostButton extends JButton {

    public GhostButton(String text) {
        super(text);
        setFont(AppFonts.body == null ? getFont() : AppFonts.body);
        setContentAreaFilled(false);
        setBorder(new EmptyBorder(AppSpacing.SM, AppSpacing.MD, AppSpacing.SM, AppSpacing.MD));
        setFocusPainted(false);
        putClientProperty("JButton.buttonType", "borderless");
    }

    // Ép chiều cao tối thiểu nhưng để width tự tính theo text (tránh cắt chữ/icon thành "...")
    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = Math.max(d.height, AppSpacing.H_INPUT);
        return d;
    }
}

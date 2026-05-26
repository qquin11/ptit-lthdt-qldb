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
        setPreferredSize(new Dimension(getPreferredSize().width, AppSpacing.H_INPUT));
        setFocusPainted(false);
        putClientProperty("JButton.buttonType", "borderless");
    }
}

package com.app.view.common;

import com.app.config.AppFonts;
import com.app.config.AppSpacing;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import java.awt.Dimension;

/** Secondary button — outlined với border + text accent. Dùng cho action phụ. */
public class SecondaryButton extends JButton {

    public SecondaryButton(String text) {
        super(text);
        setFont(AppFonts.body == null ? getFont() : AppFonts.body);
        setBorder(new EmptyBorder(AppSpacing.SM, AppSpacing.LG, AppSpacing.SM, AppSpacing.LG));
        setPreferredSize(new Dimension(getPreferredSize().width, AppSpacing.H_INPUT));
        setFocusPainted(false);
    }
}

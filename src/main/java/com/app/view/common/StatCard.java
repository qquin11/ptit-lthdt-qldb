package com.app.view.common;

import com.app.config.AppColors;
import com.app.config.AppFonts;
import com.app.config.AppSpacing;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

/**
 * KPI card — icon + value lớn + label + trend optional.
 * Dùng trên Dashboard cho "Doanh thu / Bàn dùng / HĐ".
 */
public class StatCard extends JPanel {

    private final JLabel lblValue;
    private final JLabel lblLabel;
    private final JLabel lblTrend;

    public StatCard(String iconText, String label) {
        super(new BorderLayout(AppSpacing.SM, AppSpacing.SM));
        setBorder(BorderFactory.createEmptyBorder(
                AppSpacing.LG, AppSpacing.LG, AppSpacing.LG, AppSpacing.LG));
        putClientProperty("FlatLaf.style",
                "arc: 12; background: $Table.background; border: 1,1,1,1,$Component.borderColor");
        setPreferredSize(new Dimension(220, 120));

        // Top: icon + label
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, AppSpacing.SM, 0));
        top.setOpaque(false);
        JLabel lblIcon = new JLabel(iconText);
        lblIcon.setFont(AppFonts.h1 == null ? lblIcon.getFont() : AppFonts.h1);
        lblLabel = new JLabel(label);
        lblLabel.setFont(AppFonts.small == null ? lblLabel.getFont() : AppFonts.small);
        lblLabel.setForeground(AppColors.LIGHT_TEXT_MUTED);
        top.add(lblIcon);
        top.add(lblLabel);
        add(top, BorderLayout.NORTH);

        // Center: big value
        lblValue = new JLabel("--", SwingConstants.LEFT);
        lblValue.setFont(AppFonts.display == null ? lblValue.getFont() : AppFonts.display);
        add(lblValue, BorderLayout.CENTER);

        // Bottom: trend (optional)
        lblTrend = new JLabel(" ");
        lblTrend.setFont(AppFonts.small == null ? lblTrend.getFont() : AppFonts.small);
        add(lblTrend, BorderLayout.SOUTH);
    }

    public void setValue(String value) {
        lblValue.setText(value);
    }

    /** Trend "↑ 12%" (xanh tăng) / "↓ 5%" (đỏ giảm). Null = ẩn. */
    public void setTrend(String text, boolean positive) {
        if (text == null || text.isBlank()) {
            lblTrend.setText(" ");
            return;
        }
        lblTrend.setText(text);
        lblTrend.setForeground(positive ? AppColors.SUCCESS : AppColors.DANGER);
    }

    public void setAccent(Color color) {
        lblValue.setForeground(color);
    }
}

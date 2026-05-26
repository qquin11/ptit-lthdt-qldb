package com.app.view.main;

import com.app.config.AppColors;
import com.app.config.AppFonts;
import com.app.config.AppSpacing;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * 1 mục trong sidebar — icon (emoji/text) + label.
 * 3 states: normal / hover / active. Active có accent bar bên trái.
 */
public class SidebarItem extends JPanel {

    private final String id;
    private final JLabel iconLabel;
    private final JLabel textLabel;
    private boolean active = false;
    private boolean collapsed = false;

    public SidebarItem(String id, String icon, String text, Consumer<String> onClick) {
        super(new BorderLayout(AppSpacing.MD, 0));
        this.id = id;

        setBorder(BorderFactory.createEmptyBorder(0, AppSpacing.LG, 0, AppSpacing.LG));
        setPreferredSize(new Dimension(AppSpacing.W_SIDEBAR_EXPANDED, AppSpacing.H_SIDEBAR_ITEM));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, AppSpacing.H_SIDEBAR_ITEM));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setOpaque(true);

        iconLabel = new JLabel(icon);
        iconLabel.setFont(AppFonts.icon == null ? iconLabel.getFont() : AppFonts.icon);

        textLabel = new JLabel(text);
        textLabel.setFont(AppFonts.body == null ? textLabel.getFont() : AppFonts.body);

        add(iconLabel, BorderLayout.WEST);
        add(textLabel, BorderLayout.CENTER);
        updateStyle();

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onClick.accept(id); }
            @Override public void mouseEntered(MouseEvent e) { if (!active) hoverStyle(true); }
            @Override public void mouseExited (MouseEvent e) { if (!active) hoverStyle(false); }
        });
    }

    public String getId() { return id; }

    public void setActive(boolean active) {
        this.active = active;
        updateStyle();
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
        textLabel.setVisible(!collapsed);
        setPreferredSize(new Dimension(
                collapsed ? AppSpacing.W_SIDEBAR_COLLAPSED : AppSpacing.W_SIDEBAR_EXPANDED,
                AppSpacing.H_SIDEBAR_ITEM));
        if (collapsed) {
            setToolTipText(textLabel.getText());
        } else {
            setToolTipText(null);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (active) {
            // Accent bar 3px bên trái
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(AppColors.LIGHT_ACCENT);
            g2.fillRect(0, 0, 3, getHeight());
            g2.dispose();
        }
    }

    private void hoverStyle(boolean hover) {
        setBackground(hover ? new Color(0, 0, 0, 18) : null);
        repaint();
    }

    private void updateStyle() {
        if (active) {
            setBackground(new Color(37, 99, 235, 22)); // accent tint
            textLabel.setForeground(AppColors.LIGHT_ACCENT);
            iconLabel.setForeground(AppColors.LIGHT_ACCENT);
        } else {
            setBackground(null);
            textLabel.setForeground(null);
            iconLabel.setForeground(null);
        }
        repaint();
    }
}

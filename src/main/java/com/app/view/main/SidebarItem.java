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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/** 1 mục sidebar — icon + label, 3 states (normal/hover/active). */
public class SidebarItem extends JPanel {

    private final String id;
    private final JLabel iconLabel;
    private final JLabel textLabel;
    private boolean active = false;
    private boolean hover = false;
    private boolean collapsed = false;

    public SidebarItem(String id, String icon, String text, Consumer<String> onClick) {
        super(new BorderLayout(AppSpacing.MD, 0));
        this.id = id;

        setBorder(BorderFactory.createEmptyBorder(0, AppSpacing.LG, 0, AppSpacing.LG));
        setPreferredSize(new Dimension(AppSpacing.W_SIDEBAR_EXPANDED, AppSpacing.H_SIDEBAR_ITEM));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, AppSpacing.H_SIDEBAR_ITEM));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Opaque=false để tự vẽ bg trong paintComponent — tránh alpha tích lũy gây overlap chữ
        setOpaque(false);

        iconLabel = new JLabel(icon);
        iconLabel.setFont(AppFonts.icon == null ? iconLabel.getFont() : AppFonts.icon);

        textLabel = new JLabel(text);
        textLabel.setFont(AppFonts.body == null ? textLabel.getFont() : AppFonts.body);

        add(iconLabel, BorderLayout.WEST);
        add(textLabel, BorderLayout.CENTER);
        updateColors();

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onClick.accept(id); }
            @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
            @Override public void mouseExited (MouseEvent e) { hover = false; repaint(); }
        });
    }

    public String getId() { return id; }

    public void setActive(boolean active) {
        this.active = active;
        updateColors();
        repaint();
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
        textLabel.setVisible(!collapsed);
        setPreferredSize(new Dimension(
                collapsed ? AppSpacing.W_SIDEBAR_COLLAPSED : AppSpacing.W_SIDEBAR_EXPANDED,
                AppSpacing.H_SIDEBAR_ITEM));
        setToolTipText(collapsed ? textLabel.getText() : null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Tự vẽ bg để tránh artefact khi dùng alpha trên opaque panel
        if (active) {
            g.setColor(new Color(37, 99, 235, 28));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(AppColors.LIGHT_ACCENT);
            g.fillRect(0, 0, 3, getHeight());
        } else if (hover) {
            g.setColor(new Color(0, 0, 0, 20));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        super.paintComponent(g);
    }

    private void updateColors() {
        Color fg = active ? AppColors.LIGHT_ACCENT : null;
        textLabel.setForeground(fg);
        iconLabel.setForeground(fg);
    }
}

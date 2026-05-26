package com.app.view.main;

import com.app.config.AppConfig;
import com.app.config.AppFonts;
import com.app.config.AppSpacing;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Sidebar trái — logo top + danh sách 8 mục menu + footer (Cài đặt / Đăng xuất).
 * Collapsible 220px ↔ 64px.
 */
public class Sidebar extends JPanel {

    public static final String ID_DASHBOARD   = "dashboard";
    public static final String ID_TABLES      = "tables";
    public static final String ID_RESERVATION = "reservation";
    public static final String ID_MENU        = "menu";
    public static final String ID_EMPLOYEES   = "employees";
    public static final String ID_PAYMENT     = "payment";
    public static final String ID_SETTINGS    = "settings";
    public static final String ID_LOGOUT      = "logout";

    private final Map<String, SidebarItem> items = new LinkedHashMap<>();
    private boolean collapsed = false;
    private Consumer<String> navHandler;

    public Sidebar() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(AppSpacing.W_SIDEBAR_EXPANDED, 100));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1,
                javax.swing.UIManager.getColor("Component.borderColor")));

        // Header (logo)
        JLabel logo = new JLabel("🍽  " + AppConfig.APP_NAME);
        logo.setFont(AppFonts.h2 == null ? logo.getFont() : AppFonts.h2);
        logo.setHorizontalAlignment(SwingConstants.LEFT);
        logo.setBorder(BorderFactory.createEmptyBorder(
                AppSpacing.LG, AppSpacing.LG, AppSpacing.LG, AppSpacing.LG));
        add(logo, BorderLayout.NORTH);

        // Menu + footer
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        addItem(body, ID_DASHBOARD,   "🏠", "Trang chủ");
        addItem(body, ID_TABLES,      "🗺", "Sơ đồ bàn");
        addItem(body, ID_RESERVATION, "🛎", "Đặt bàn");
        addItem(body, ID_MENU,        "🍱", "Menu");
        addItem(body, ID_EMPLOYEES,   "👥", "Nhân viên");
        addItem(body, ID_PAYMENT,     "💳", "Thanh toán");

        body.add(Box.createVerticalGlue());
        body.add(new JSeparator(SwingConstants.HORIZONTAL));
        addItem(body, ID_SETTINGS, "⚙", "Cài đặt");
        addItem(body, ID_LOGOUT,   "🚪", "Đăng xuất");

        add(body, BorderLayout.CENTER);
    }

    private void addItem(JPanel parent, String id, String icon, String text) {
        SidebarItem item = new SidebarItem(id, icon, text, this::handleClick);
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        items.put(id, item);
        parent.add(item);
    }

    private void handleClick(String id) {
        if (!ID_SETTINGS.equals(id) && !ID_LOGOUT.equals(id)) {
            setActive(id);
        }
        if (navHandler != null) navHandler.accept(id);
    }

    public void setActive(String id) {
        items.values().forEach(it -> it.setActive(false));
        SidebarItem target = items.get(id);
        if (target != null) target.setActive(true);
    }

    public void setOnNavigate(Consumer<String> handler) {
        this.navHandler = handler;
    }

    public void toggleCollapsed() {
        collapsed = !collapsed;
        setPreferredSize(new Dimension(
                collapsed ? AppSpacing.W_SIDEBAR_COLLAPSED : AppSpacing.W_SIDEBAR_EXPANDED, 100));
        items.values().forEach(it -> it.setCollapsed(collapsed));
        revalidate();
        repaint();
    }

    public List<String> ids() {
        return new ArrayList<>(items.keySet());
    }
}

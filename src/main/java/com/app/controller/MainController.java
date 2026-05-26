package com.app.controller;

import com.app.config.Session;
import com.app.config.ThemeManager;
import com.app.view.common.ConfirmDialog;
import com.app.view.common.Toast;
import com.app.view.main.MainFrame;
import com.app.view.main.Sidebar;
import com.app.view.main.UserMenuPopup;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

/**
 * Wire Sidebar navigation + TopBar actions + theme changes cho MainFrame.
 */
public class MainController {

    private final MainFrame view;
    private Runnable onLogout = () -> {};

    public MainController(MainFrame view) {
        this.view = view;

        // Sidebar navigation
        Sidebar sidebar = view.getSidebarPanel();
        sidebar.setOnNavigate(this::handleNav);

        // TopBar user menu
        view.getTopBarPanel().setOnUserMenu(this::showUserMenu);

        // Refresh UI khi theme đổi (cập nhật icons custom)
        ThemeManager.addThemeChangeListener(t -> {
            view.getTopBarPanel().refreshUser();
            view.getStatusBar().refreshUser();
        });

        // Default: show Dashboard
        view.showPanel(Sidebar.ID_DASHBOARD);
    }

    public void setOnLogout(Runnable handler) {
        this.onLogout = handler == null ? () -> {} : handler;
    }

    private void handleNav(String id) {
        switch (id) {
            case Sidebar.ID_LOGOUT -> logout();
            case Sidebar.ID_SETTINGS -> Toast.info(view, "Cài đặt: tính năng sẽ có ở Phase 2");
            default -> view.showPanel(id);
        }
    }

    private void showUserMenu() {
        JPopupMenu menu = UserMenuPopup.build(
                e -> Toast.info(view, "Hồ sơ: " + Session.currentName()),
                e -> Toast.info(view, "Đổi mật khẩu: tính năng sẽ có ở Phase 2"),
                e -> logout());
        UserMenuPopup.showBelow(menu, view.getTopBarPanel().getUserButton());
    }

    private void logout() {
        boolean ok = ConfirmDialog.ask(view, "Đăng xuất",
                "Bạn có chắc muốn đăng xuất khỏi hệ thống?",
                "Đăng xuất", "Hủy", false);
        if (!ok) return;
        Session.clear();
        view.dispose();
        onLogout.run();
    }
}

package com.app.controller;

import com.app.config.Session;
import com.app.config.ThemeManager;
import com.app.view.auth.ChangePasswordDialog;
import com.app.view.common.ConfirmDialog;
import com.app.view.common.Toast;
import com.app.view.main.MainFrame;
import com.app.view.main.Sidebar;
import com.app.view.main.UserMenuPopup;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import java.awt.Frame;

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

        // TopBar user menu + sidebar toggle
        view.getTopBarPanel().setOnUserMenu(this::showUserMenu);
        view.getTopBarPanel().setOnToggleSidebar(() -> view.getSidebarPanel().toggleCollapsed());

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
            case Sidebar.ID_SETTINGS -> Toast.info(view, "Cài đặt: sẽ bổ sung sau");
            default -> view.showPanel(id);
        }
    }

    private void showUserMenu() {
        JPopupMenu menu = UserMenuPopup.build(
                e -> Toast.info(view, "Hồ sơ: " + Session.currentName() + " · " + Session.currentRole()),
                e -> openChangePassword(),
                e -> logout());
        UserMenuPopup.showBelow(menu, view.getTopBarPanel().getUserButton());
    }

    private void openChangePassword() {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(view);
        new ChangePasswordDialog(owner).setVisible(true);
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

package com.app.view.main;

import com.app.config.AppFonts;
import com.app.config.AppSpacing;
import com.app.config.Session;
import com.app.config.ThemeManager;
import com.app.view.common.GhostButton;
import com.app.view.common.SearchField;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * TopBar 56px — breadcrumb (left) + search (center) + actions (right: theme toggle, user menu).
 */
public class TopBar extends JPanel {

    private final JLabel breadcrumb;
    private final SearchField search;
    private final GhostButton btnTheme;
    private final GhostButton btnUser;
    private Supplier<Component> parentForUserMenu = () -> this;

    public TopBar() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(100, AppSpacing.H_TOPBAR));
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                javax.swing.UIManager.getColor("Component.borderColor")));

        // Left: breadcrumb
        breadcrumb = new JLabel("Trang chủ", SwingConstants.LEFT);
        breadcrumb.setFont(AppFonts.h2 == null ? breadcrumb.getFont() : AppFonts.h2);
        breadcrumb.setBorder(BorderFactory.createEmptyBorder(0, AppSpacing.LG, 0, AppSpacing.LG));
        add(breadcrumb, BorderLayout.WEST);

        // Center: search
        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, AppSpacing.SM));
        center.setOpaque(false);
        search = new SearchField("Tìm kiếm... (Ctrl+K)");
        center.add(search);
        add(center, BorderLayout.CENTER);

        // Right: actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppSpacing.SM, AppSpacing.SM));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, AppSpacing.MD));

        btnTheme = new GhostButton(themeIcon());
        btnTheme.setToolTipText("Đổi theme (Light/Dark)");
        btnTheme.addActionListener(e -> {
            ThemeManager.toggle();
            btnTheme.setText(themeIcon());
        });

        btnUser = new GhostButton("👤 " + Session.currentName());
        btnUser.setToolTipText("Tài khoản");
        btnUser.addActionListener(e -> showUserMenu());

        actions.add(btnTheme);
        actions.add(btnUser);
        add(actions, BorderLayout.EAST);
    }

    /** Update breadcrumb khi chuyển panel */
    public void setBreadcrumb(String text) {
        breadcrumb.setText(text);
    }

    public void refreshUser() {
        btnUser.setText("👤 " + Session.currentName());
    }

    public void onSearch(Consumer<String> listener) {
        search.onTextChanged(listener);
    }

    public void setOnUserMenu(Runnable openMenu) {
        this.userMenuHandler = openMenu;
    }

    private Runnable userMenuHandler;

    private void showUserMenu() {
        if (userMenuHandler != null) userMenuHandler.run();
    }

    private String themeIcon() {
        return ThemeManager.current() == ThemeManager.Theme.DARK ? "☀" : "🌙";
    }

    public GhostButton getUserButton() { return btnUser; }
}

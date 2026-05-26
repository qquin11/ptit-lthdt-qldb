package com.app.view.main;

import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import java.awt.Component;
import java.awt.Color;
import java.awt.event.ActionListener;

/** Dropdown menu cho user button trên TopBar. */
public class UserMenuPopup {

    public static JPopupMenu build(ActionListener onProfile,
                                    ActionListener onChangePassword,
                                    ActionListener onLogout) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem mProfile = new JMenuItem("Hồ sơ của tôi");
        JMenuItem mChange  = new JMenuItem("Đổi mật khẩu");
        JMenuItem mLogout  = new JMenuItem("Đăng xuất");
        mLogout.setForeground(new Color(0xEF4444));

        if (onProfile != null) mProfile.addActionListener(onProfile);
        if (onChangePassword != null) mChange.addActionListener(onChangePassword);
        if (onLogout != null) mLogout.addActionListener(onLogout);

        menu.add(mProfile);
        menu.add(mChange);
        menu.addSeparator();
        menu.add(mLogout);
        return menu;
    }

    public static void showBelow(JPopupMenu menu, Component anchor) {
        menu.show(anchor, 0, anchor.getHeight());
    }
}

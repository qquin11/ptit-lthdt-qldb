package com.app;

import com.app.config.ThemeManager;
import com.app.controller.LoginController;
import com.app.controller.MainController;
import com.app.model.DatabaseHelper;
import com.app.view.auth.LoginFrame;
import com.app.view.main.MainFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Entry point ResMan POS.
 *
 * <p>Thứ tự khởi tạo:
 * <ol>
 *   <li>{@link ThemeManager#init()} — FlatLaf theme + fonts (TRƯỚC mọi Swing component)</li>
 *   <li>{@link DatabaseHelper#getConnection()} — init/migrate SQLite</li>
 *   <li>{@link LoginFrame} → on success → {@link MainFrame}</li>
 *   <li>Logout từ MainFrame → quay về LoginFrame (loop)</li>
 * </ol>
 */
public class Main {

    public static void main(String[] args) {
        // Theme TRƯỚC khi tạo bất kỳ Swing component
        ThemeManager.init();
        SwingUtilities.invokeLater(Main::startUi);
    }

    private static void startUi() {
        // Init DB (auto-create schema + migration lần đầu)
        if (DatabaseHelper.getConnection() == null) {
            JOptionPane.showMessageDialog(null,
                    "Lỗi: Không thể kết nối đến cơ sở dữ liệu!",
                    "Lỗi Nghiêm Trọng", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        openLogin();
    }

    /** Hiển thị Login → sau khi auth OK → mở MainFrame. */
    private static void openLogin() {
        LoginFrame login = new LoginFrame();
        LoginController loginController = new LoginController(login);
        loginController.setOnLoginSuccess(user -> {
            login.dispose();
            openMain();
        });
        login.setVisible(true);
    }

    /** Sau login → MainFrame. Logout → quay về Login. */
    private static void openMain() {
        MainFrame frame = new MainFrame();
        MainController controller = new MainController(frame);
        controller.setOnLogout(Main::openLogin);
        frame.setVisible(true);
    }
}

package com.app;

import com.app.controller.MainController;
import com.app.model.DatabaseHelper;
import com.app.view.MainFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        System.out.println("Đang khởi tạo kết nối cơ sở dữ liệu...");
        java.sql.Connection conn = com.app.model.DatabaseHelper.getConnection();

        // Nếu kết nối thành công (conn không null), hiển thị hộp thoại thông báo trực quan
        if (conn != null) {
            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "Kết nối cơ sở dữ liệu SQLite thành công!\nHệ thống đã sẵn sàng.",
                    "Thông Báo Hệ Thống",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE
            );
        } else {
            // Trường hợp lỗi kết nối, cảnh báo cho người dùng biết
            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "Lỗi: Không thể kết nối đến cơ sở dữ liệu!\nVui lòng kiểm tra lại hệ thống.",
                    "Lỗi Nghiêm Trọng",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                MainFrame frame = new MainFrame();
                new MainController(frame);
                frame.setVisible(true);
            }
        });
    }
}
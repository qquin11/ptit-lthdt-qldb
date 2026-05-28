package com.app.view.main;

import com.app.config.AppFonts;
import com.app.config.AppSpacing;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Frame;

/** Help dialog hiển thị danh sách keyboard shortcuts (F1 trigger). */
public class HelpDialog extends JDialog {

    private static final String CONTENT = """
            Phím tắt:
            -----------------------------------
              F1            Mở help (cửa sổ này)
              F5            Reload màn hình hiện tại
              Ctrl + K      Focus ô tìm kiếm
              Enter         Submit form
              Esc           Đóng dialog / hủy

            Login:
              Tab           Chuyển field
              Enter         Đăng nhập

            Order screen:
              Ctrl + S      Lưu nháp
              F9            Chuyển sang thanh toán

            Sơ đồ bàn:
              Click bàn     Mở order panel
              Right-click   Menu (Order/Đặt/Thanh toán)

            -----------------------------------
            Mẹo: theme Light/Dark đổi qua nút
            trên TopBar (góc trên bên phải).
            """;

    public HelpDialog(Frame owner) {
        super(owner, "Phím tắt & Trợ giúp", true);
        setSize(440, 520);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Phím tắt");
        title.setFont(AppFonts.h1 == null ? title.getFont() : AppFonts.h1);
        title.setBorder(BorderFactory.createEmptyBorder(
                AppSpacing.LG, AppSpacing.LG, AppSpacing.SM, AppSpacing.LG));
        add(title, BorderLayout.NORTH);

        JTextArea text = new JTextArea(CONTENT);
        text.setEditable(false);
        text.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 13));
        text.setBorder(BorderFactory.createEmptyBorder(
                AppSpacing.SM, AppSpacing.LG, AppSpacing.LG, AppSpacing.LG));
        add(new JScrollPane(text), BorderLayout.CENTER);
    }
}

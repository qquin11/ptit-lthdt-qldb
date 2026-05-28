package com.app.view.auth;

import com.app.config.AppFonts;
import com.app.config.AppSpacing;
import com.app.config.Session;
import com.app.service.AuthService;
import com.app.util.PasswordHasher;
import com.app.view.common.PrimaryButton;
import com.app.view.common.SecondaryButton;
import com.app.view.common.Toast;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;

/** Modal đổi mật khẩu cho user đang đăng nhập. */
public class ChangePasswordDialog extends JDialog {

    private final JPasswordField fieldOld = new JPasswordField();
    private final JPasswordField fieldNew = new JPasswordField();
    private final JPasswordField fieldConfirm = new JPasswordField();
    private final JLabel lblError = new JLabel(" ");

    private final AuthService authService = new AuthService();

    public ChangePasswordDialog(Frame owner) {
        super(owner, "Đổi mật khẩu", true);
        setSize(380, 320);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        add(buildForm(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);
        SwingUtilities.invokeLater(fieldOld::requestFocusInWindow);
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridLayout(0, 1, 0, AppSpacing.XS));
        form.setBorder(BorderFactory.createEmptyBorder(
                AppSpacing.LG, AppSpacing.LG, AppSpacing.MD, AppSpacing.LG));

        form.add(label("Mật khẩu hiện tại *"));
        form.add(fieldOld);
        form.add(label("Mật khẩu mới *"));
        form.add(fieldNew);
        form.add(label("Nhập lại mật khẩu mới *"));
        form.add(fieldConfirm);

        lblError.setForeground(new java.awt.Color(0xEF4444));
        lblError.setFont(AppFonts.small == null ? lblError.getFont() : AppFonts.small);
        lblError.setPreferredSize(new Dimension(0, 16));
        form.add(lblError);
        return form;
    }

    private JPanel buildButtons() {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppSpacing.SM, AppSpacing.SM));
        buttons.setBorder(BorderFactory.createEmptyBorder(0, AppSpacing.LG, AppSpacing.LG, AppSpacing.LG));
        SecondaryButton btnCancel = new SecondaryButton("Hủy");
        btnCancel.addActionListener(e -> dispose());
        PrimaryButton btnSubmit = new PrimaryButton("Đổi mật khẩu");
        btnSubmit.addActionListener(e -> submit());
        buttons.add(btnCancel);
        buttons.add(btnSubmit);
        return buttons;
    }

    private void submit() {
        lblError.setText(" ");
        var user = Session.get();
        if (user == null) { lblError.setText("Chưa đăng nhập"); return; }

        String oldPwd = new String(fieldOld.getPassword());
        String newPwd = new String(fieldNew.getPassword());
        String confirmPwd = new String(fieldConfirm.getPassword());

        if (!PasswordHasher.verify(oldPwd, user.getMatKhau())) {
            lblError.setText("Mật khẩu hiện tại không đúng");
            return;
        }
        if (newPwd.length() < 6) {
            lblError.setText("Mật khẩu mới phải từ 6 ký tự");
            return;
        }
        if (!newPwd.equals(confirmPwd)) {
            lblError.setText("Mật khẩu nhập lại không khớp");
            return;
        }
        if (newPwd.equals(oldPwd)) {
            lblError.setText("Mật khẩu mới phải khác mật khẩu cũ");
            return;
        }

        try {
            authService.changePassword(user, newPwd);
            Toast.success(this, "Đổi mật khẩu thành công");
            dispose();
        } catch (Exception ex) {
            lblError.setText(ex.getMessage());
        }
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppFonts.small == null ? l.getFont() : AppFonts.small);
        return l;
    }
}

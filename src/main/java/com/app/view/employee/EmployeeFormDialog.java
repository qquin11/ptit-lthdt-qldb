package com.app.view.employee;

import com.app.config.AppFonts;
import com.app.config.AppSpacing;
import com.app.model.NhanVien;
import com.app.service.EmployeeService;
import com.app.util.ValidationUtils;
import com.app.view.common.PrimaryButton;
import com.app.view.common.SecondaryButton;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.function.BiConsumer;

/**
 * Modal Add/Edit nhân viên. Password required khi add, optional khi edit.
 */
public class EmployeeFormDialog extends JDialog {

    private final JTextField fieldUsername = new JTextField();
    private final JTextField fieldName = new JTextField();
    private final JPasswordField fieldPassword = new JPasswordField();
    private final JComboBox<String> comboRole = new JComboBox<>(new String[]{"Nhân viên", "Quản lý"});

    private final JLabel errUsername = newError();
    private final JLabel errName = newError();
    private final JLabel errPassword = newError();

    private final NhanVien editing;
    private BiConsumer<NhanVien, String> onSave = (nv, pwd) -> {};

    public EmployeeFormDialog(Frame owner, NhanVien editing) {
        super(owner, editing == null ? "Thêm nhân viên" : "Sửa nhân viên #" + editing.getId(), true);
        this.editing = editing;
        setSize(420, 420);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        add(buildForm(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        if (editing != null) prefill(editing);
        SwingUtilities.invokeLater(fieldName::requestFocusInWindow);
    }

    public void onSave(BiConsumer<NhanVien, String> handler) { this.onSave = handler; }

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setLayout(new GridLayout(0, 1, 0, AppSpacing.XS));
        form.setBorder(BorderFactory.createEmptyBorder(
                AppSpacing.LG, AppSpacing.LG, AppSpacing.MD, AppSpacing.LG));

        form.add(label("Họ tên *"));
        form.add(fieldName);
        form.add(errName);

        form.add(label("Tài khoản *"));
        form.add(fieldUsername);
        form.add(errUsername);

        form.add(label(editing == null ? "Mật khẩu *" : "Mật khẩu (để trống nếu không đổi)"));
        form.add(fieldPassword);
        form.add(errPassword);

        form.add(label("Vai trò *"));
        form.add(comboRole);
        return form;
    }

    private JPanel buildButtons() {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppSpacing.SM, AppSpacing.SM));
        buttons.setBorder(BorderFactory.createEmptyBorder(0, AppSpacing.LG, AppSpacing.LG, AppSpacing.LG));

        SecondaryButton btnCancel = new SecondaryButton("Hủy");
        btnCancel.addActionListener(e -> dispose());
        PrimaryButton btnSave = new PrimaryButton("Lưu");
        btnSave.addActionListener(e -> trySave());

        buttons.add(btnCancel);
        buttons.add(btnSave);
        return buttons;
    }

    private void prefill(NhanVien n) {
        fieldUsername.setText(n.getTaiKhoan());
        fieldUsername.setEditable(false); // Không cho đổi username khi edit
        fieldName.setText(n.getHoTen());
        comboRole.setSelectedItem("ADMIN".equals(n.getVaiTro()) ? "Quản lý" : "Nhân viên");
    }

    private void trySave() {
        errName.setText(" ");
        errUsername.setText(" ");
        errPassword.setText(" ");

        String name = fieldName.getText().trim();
        String username = fieldUsername.getText().trim();
        String pwd = new String(fieldPassword.getPassword());

        boolean ok = true;
        if (!ValidationUtils.isRequired(name)) {
            errName.setText("Họ tên bắt buộc"); ok = false;
        }
        if (editing == null) {
            if (!ValidationUtils.isUsername(username)) {
                errUsername.setText("Tài khoản 3-30 ký tự, chỉ chữ/số/_"); ok = false;
            }
            if (pwd.length() < 6) {
                errPassword.setText("Mật khẩu tối thiểu 6 ký tự"); ok = false;
            }
        } else if (!pwd.isEmpty() && pwd.length() < 6) {
            errPassword.setText("Mật khẩu tối thiểu 6 ký tự"); ok = false;
        }
        if (!ok) return;

        NhanVien target = editing != null ? editing : new NhanVien();
        target.setHoTen(name);
        target.setTaiKhoan(username);
        target.setVaiTro("Quản lý".equals(comboRole.getSelectedItem())
                ? EmployeeService.ROLE_ADMIN : EmployeeService.ROLE_STAFF);
        if (editing == null) {
            target.setTrangThai(1);
        }
        onSave.accept(target, pwd);
        dispose();
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppFonts.small == null ? l.getFont() : AppFonts.small);
        return l;
    }

    private static JLabel newError() {
        JLabel l = new JLabel(" ");
        l.setForeground(new java.awt.Color(0xEF4444));
        l.setFont(l.getFont().deriveFont(11f));
        l.setPreferredSize(new Dimension(0, 16));
        return l;
    }
}

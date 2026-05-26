package com.app.view.auth;

import com.app.config.AppColors;
import com.app.config.AppConfig;
import com.app.config.AppFonts;
import com.app.config.AppSpacing;
import com.app.view.common.GhostButton;
import com.app.view.common.PrimaryButton;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.function.BiConsumer;

/**
 * Login screen — branded 400×500. Validation inline, Enter submit,
 * show/hide password, remember me persist.
 */
public class LoginFrame extends JFrame {

    private final JTextField fieldUsername = new JTextField(20);
    private final JPasswordField fieldPassword = new JPasswordField(20);
    private final JCheckBox cbRemember = new JCheckBox("Ghi nhớ tôi");
    private final GhostButton btnEye = new GhostButton("◉");
    private final PrimaryButton btnLogin = new PrimaryButton("ĐĂNG NHẬP");
    private final JLabel lblError = new JLabel(" ");
    private boolean passwordVisible = false;

    private BiConsumer<String, char[]> submitHandler = (u, p) -> {};

    public LoginFrame() {
        setTitle(AppConfig.APP_NAME + " — Đăng nhập");
        setSize(AppConfig.LOGIN_WIDTH, AppConfig.LOGIN_HEIGHT);
        setMinimumSize(new Dimension(360, 460));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        wireSubmitOnEnter();
        btnLogin.addActionListener(e -> trySubmit());
        btnEye.addActionListener(e -> togglePassword());
    }

    // ===================== UI BUILDING =====================

    private JPanel buildHeader() {
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(AppSpacing.XXL, 0, AppSpacing.MD, 0));

        JLabel logo = new JLabel("✦", SwingConstants.CENTER);
        logo.setFont(logo.getFont().deriveFont(java.awt.Font.BOLD, 64f));
        logo.setForeground(AppColors.LIGHT_ACCENT);

        JLabel brand = new JLabel(AppConfig.APP_NAME, SwingConstants.CENTER);
        brand.setFont(AppFonts.display == null ? brand.getFont() : AppFonts.display);
        brand.setForeground(AppColors.LIGHT_ACCENT);

        header.add(logo);
        header.add(brand);
        return header;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setLayout(new GridLayout(0, 1, 0, AppSpacing.SM));
        form.setBorder(BorderFactory.createEmptyBorder(
                AppSpacing.LG, AppSpacing.XXL, AppSpacing.LG, AppSpacing.XXL));

        JLabel lblTitle = new JLabel("Đăng nhập hệ thống", SwingConstants.CENTER);
        lblTitle.setFont(AppFonts.h2 == null ? lblTitle.getFont() : AppFonts.h2);
        form.add(lblTitle);

        // Username row
        form.add(new JLabel("Tài khoản"));
        fieldUsername.putClientProperty("JTextField.placeholderText", "Nhập tài khoản (vd: admin)");
        fieldUsername.setPreferredSize(new Dimension(0, AppSpacing.H_INPUT));
        form.add(fieldUsername);

        // Password row + eye toggle (inner panel)
        form.add(new JLabel("Mật khẩu"));
        JPanel pwdRow = new JPanel(new BorderLayout(AppSpacing.XS, 0));
        pwdRow.setOpaque(false);
        fieldPassword.putClientProperty("JTextField.placeholderText", "••••••••");
        fieldPassword.setPreferredSize(new Dimension(0, AppSpacing.H_INPUT));
        btnEye.setToolTipText("Hiện/ẩn mật khẩu");
        pwdRow.add(fieldPassword, BorderLayout.CENTER);
        pwdRow.add(btnEye, BorderLayout.EAST);
        form.add(pwdRow);

        // Remember me + forgot
        JPanel options = new JPanel(new BorderLayout());
        options.setOpaque(false);
        options.add(cbRemember, BorderLayout.WEST);
        GhostButton btnForgot = new GhostButton("Quên mật khẩu?");
        btnForgot.addActionListener(e -> javax.swing.JOptionPane.showMessageDialog(this,
                "Liên hệ quản lý để được đặt lại mật khẩu.\n\nMặc định:\nadmin / admin123\nstaff / staff123",
                "Quên mật khẩu", javax.swing.JOptionPane.INFORMATION_MESSAGE));
        options.add(btnForgot, BorderLayout.EAST);
        form.add(options);

        // Submit button
        form.add(btnLogin);

        // Error label
        lblError.setForeground(AppColors.DANGER);
        lblError.setHorizontalAlignment(SwingConstants.CENTER);
        lblError.setFont(AppFonts.small == null ? lblError.getFont() : AppFonts.small);
        form.add(lblError);

        // Clear error khi user gõ
        DocumentListener clear = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { lblError.setText(" "); }
            @Override public void removeUpdate(DocumentEvent e) { lblError.setText(" "); }
            @Override public void changedUpdate(DocumentEvent e) { lblError.setText(" "); }
        };
        fieldUsername.getDocument().addDocumentListener(clear);
        fieldPassword.getDocument().addDocumentListener(clear);

        return form;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, AppSpacing.SM));
        footer.setOpaque(false);
        JLabel ver = new JLabel("v" + AppConfig.APP_VERSION + " · " + AppConfig.APP_VENDOR);
        ver.setFont(AppFonts.small == null ? ver.getFont() : AppFonts.small);
        ver.setForeground(AppColors.LIGHT_TEXT_MUTED);
        footer.add(ver);
        return footer;
    }

    private void wireSubmitOnEnter() {
        fieldUsername.addActionListener(e -> fieldPassword.requestFocusInWindow());
        fieldPassword.addActionListener(e -> trySubmit());
    }

    private void trySubmit() {
        if (submitHandler != null) {
            submitHandler.accept(fieldUsername.getText().trim(), fieldPassword.getPassword());
        }
    }

    private void togglePassword() {
        passwordVisible = !passwordVisible;
        fieldPassword.setEchoChar(passwordVisible ? (char) 0 : '•');
        btnEye.setText(passwordVisible ? "○" : "◉");
    }

    // ===================== PUBLIC API CHO CONTROLLER =====================

    public void onSubmit(BiConsumer<String, char[]> handler) {
        this.submitHandler = handler;
    }

    public void setError(String msg) {
        lblError.setText(msg == null ? " " : msg);
    }

    /** Đổi trạng thái loading: disable form khi đang auth */
    public void setBusy(boolean busy) {
        fieldUsername.setEnabled(!busy);
        fieldPassword.setEnabled(!busy);
        btnLogin.setEnabled(!busy);
        cbRemember.setEnabled(!busy);
        btnLogin.setText(busy ? "Đang xác thực..." : "ĐĂNG NHẬP");
    }

    /** Lock form X giây sau 3 lần sai. Hiển thị countdown trên button. */
    public void setLocked(boolean locked, int seconds) {
        fieldUsername.setEnabled(!locked);
        fieldPassword.setEnabled(!locked);
        btnLogin.setEnabled(!locked);
        if (locked) {
            btnLogin.setText("Khóa: thử lại sau " + seconds + "s");
            lblError.setText("Sai quá nhiều lần. Hãy chờ " + seconds + "s.");
        } else {
            btnLogin.setText("ĐĂNG NHẬP");
        }
    }

    public void clearPassword() {
        fieldPassword.setText("");
        fieldPassword.requestFocusInWindow();
    }

    public String getUsername() { return fieldUsername.getText().trim(); }
    public void setUsername(String s) { fieldUsername.setText(s); }
    public boolean isRememberChecked() { return cbRemember.isSelected(); }
    public void setRememberChecked(boolean b) { cbRemember.setSelected(b); }
}

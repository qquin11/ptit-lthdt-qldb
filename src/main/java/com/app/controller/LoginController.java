package com.app.controller;

import com.app.config.AppConfig;
import com.app.config.Session;
import com.app.model.NhanVien;
import com.app.service.AuthService;
import com.app.util.SwingWorkerHelper;
import com.app.view.auth.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

/**
 * Wire LoginFrame với AuthService. Quản lý:
 * <ul>
 *   <li>Validation input cơ bản (required)</li>
 *   <li>Async auth qua SwingWorker</li>
 *   <li>Counter sai 3 lần → lock 30s</li>
 *   <li>Remember username persist Preferences</li>
 *   <li>Success → Session.set + callback {@link #setOnLoginSuccess}</li>
 * </ul>
 */
public class LoginController {

    private static final Preferences PREFS = Preferences.userNodeForPackage(LoginController.class);

    private final LoginFrame view;
    private final AuthService authService = new AuthService();
    private Consumer<NhanVien> onSuccess = nv -> {};

    private int failedAttempts = 0;

    public LoginController(LoginFrame view) {
        this.view = view;
        view.onSubmit(this::handleSubmit);

        // Restore remembered username
        String remembered = PREFS.get(AppConfig.PREF_USERNAME, "");
        if (!remembered.isEmpty()) {
            view.setUsername(remembered);
            view.setRememberChecked(true);
        }
    }

    public void setOnLoginSuccess(Consumer<NhanVien> handler) {
        this.onSuccess = handler == null ? nv -> {} : handler;
    }

    // INTERNAL

    private void handleSubmit(String username, char[] password) {
        // Validate input
        if (username.isEmpty()) {
            view.setError("Vui lòng nhập tài khoản");
            return;
        }
        if (password.length == 0) {
            view.setError("Vui lòng nhập mật khẩu");
            return;
        }

        view.setBusy(true);
        String pwd = new String(password);

        SwingWorkerHelper.run(
                () -> authService.login(username, pwd),
                this::handleAuthResult,
                err -> {
                    view.setBusy(false);
                    view.setError("Lỗi: " + err.getMessage());
                });
    }

    private void handleAuthResult(Optional<NhanVien> result) {
        view.setBusy(false);
        if (result.isEmpty()) {
            failedAttempts++;
            view.clearPassword();
            if (failedAttempts >= AppConfig.LOGIN_MAX_ATTEMPTS) {
                lockTemporarily();
            } else {
                view.setError("Sai tài khoản hoặc mật khẩu (" + failedAttempts
                        + "/" + AppConfig.LOGIN_MAX_ATTEMPTS + ")");
            }
            return;
        }

        // Success
        failedAttempts = 0;
        NhanVien user = result.get();
        Session.set(user);

        // Remember me
        if (view.isRememberChecked()) {
            PREFS.put(AppConfig.PREF_USERNAME, user.getTaiKhoan());
        } else {
            PREFS.remove(AppConfig.PREF_USERNAME);
        }

        SwingUtilities.invokeLater(() -> onSuccess.accept(user));
    }

    private void lockTemporarily() {
        int totalSec = AppConfig.LOGIN_LOCK_SECONDS;
        view.setLocked(true, totalSec);

        // Countdown
        int[] remaining = { totalSec };
        Timer timer = new Timer(1000, null);
        timer.addActionListener(e -> {
            remaining[0]--;
            if (remaining[0] <= 0) {
                timer.stop();
                failedAttempts = 0;
                view.setLocked(false, 0);
                view.setError(" ");
            } else {
                view.setLocked(true, remaining[0]);
            }
        });
        timer.start();
    }
}

package com.app.service;

import com.app.dao.NhanVienDAO;
import com.app.model.NhanVien;
import com.app.util.PasswordHasher;

import java.util.Optional;

/**
 * Auth service — login với hash verify, auto-rehash legacy plain text passwords.
 * Dùng table nhan_vien trực tiếp (không có table tai_khoan riêng).
 */
public class AuthService {

    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();

    /** Login: trả về NhanVien nếu OK, empty nếu sai. */
    public Optional<NhanVien> login(String taiKhoan, String plainPassword) {
        if (taiKhoan == null || taiKhoan.isBlank() || plainPassword == null) {
            return Optional.empty();
        }
        // Tìm theo tài khoản (không match password ngay — kiểm tra qua hasher)
        NhanVien nv = findByUsername(taiKhoan.trim());
        if (nv == null || nv.getTrangThai() != 1) return Optional.empty();

        String stored = nv.getMatKhau();
        if (!PasswordHasher.verify(plainPassword, stored)) {
            return Optional.empty();
        }

        // Auto-rehash legacy plain text → hash format (one-time migration on first login)
        if (PasswordHasher.needsRehash(stored)) {
            String newHash = PasswordHasher.hash(plainPassword);
            nv.setMatKhau(newHash);
            nhanVienDAO.update(nv);
            System.out.println(">>> Migrated password hash for: " + taiKhoan);
        }

        return Optional.of(nv);
    }

    /** Đổi mật khẩu — caller phải verify old password trước. */
    public boolean changePassword(NhanVien nv, String newPlain) {
        if (newPlain == null || newPlain.length() < 6) {
            throw new DataAccessException("Mật khẩu phải tối thiểu 6 ký tự");
        }
        nv.setMatKhau(PasswordHasher.hash(newPlain));
        return nhanVienDAO.update(nv);
    }

    private NhanVien findByUsername(String taiKhoan) {
        return nhanVienDAO.findAll().stream()
                .filter(n -> taiKhoan.equalsIgnoreCase(n.getTaiKhoan()))
                .findFirst().orElse(null);
    }
}

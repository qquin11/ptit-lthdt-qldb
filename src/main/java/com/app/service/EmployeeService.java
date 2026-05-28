package com.app.service;

import com.app.dao.NhanVienDAO;
import com.app.model.NhanVien;
import com.app.util.PasswordHasher;
import com.app.util.ValidationUtils;

import java.util.List;

/** CRUD nhân viên + reset password. */
public class EmployeeService {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_STAFF = "STAFF";

    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();

    public List<NhanVien> listAll() { return nhanVienDAO.findAll(); }

    public NhanVien create(NhanVien nv, String plainPassword) {
        validate(nv, true);
        if (plainPassword == null || plainPassword.length() < 6) {
            throw new DataAccessException("Mật khẩu phải tối thiểu 6 ký tự");
        }
        // Username unique
        boolean exists = nhanVienDAO.findAll().stream()
                .anyMatch(n -> nv.getTaiKhoan().equalsIgnoreCase(n.getTaiKhoan()));
        if (exists) throw new DataAccessException("Tài khoản đã tồn tại");

        nv.setMatKhau(PasswordHasher.hash(plainPassword));
        nv.setTrangThai(1);
        boolean ok = nhanVienDAO.insert(nv);
        if (!ok) throw new DataAccessException("Tạo nhân viên thất bại");
        return nv;
    }

    public boolean update(NhanVien nv) {
        validate(nv, false);
        return nhanVienDAO.update(nv);
    }

    /** Soft delete: set trangThai = 0 */
    public boolean deactivate(int id) { return nhanVienDAO.delete(id); }

    public boolean resetPassword(NhanVien nv, String newPlain) {
        if (newPlain == null || newPlain.length() < 6) {
            throw new DataAccessException("Mật khẩu mới phải tối thiểu 6 ký tự");
        }
        nv.setMatKhau(PasswordHasher.hash(newPlain));
        return nhanVienDAO.update(nv);
    }

    private void validate(NhanVien nv, boolean checkUsername) {
        if (nv == null) throw new DataAccessException("Nhân viên không hợp lệ");
        if (!ValidationUtils.isRequired(nv.getHoTen())) throw new DataAccessException("Họ tên bắt buộc");
        if (checkUsername && !ValidationUtils.isUsername(nv.getTaiKhoan())) {
            throw new DataAccessException("Tài khoản 3-30 ký tự, chỉ chữ/số/_");
        }
        if (!ROLE_ADMIN.equals(nv.getVaiTro()) && !ROLE_STAFF.equals(nv.getVaiTro())) {
            throw new DataAccessException("Vai trò phải là ADMIN hoặc STAFF");
        }
    }
}

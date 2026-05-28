package com.app.dao;

import com.app.model.NhanVien;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDAO extends BaseDAO {

    public NhanVien checkLogin(String taiKhoan, String matKhau) {
        String sql = "SELECT * FROM nhan_vien WHERE tai_khoan = ? AND mat_khau = ? AND trang_thai = 1";
        try (Connection conn = getConnection();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setString(1, taiKhoan);
            pstm.setString(2, matKhau);

            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    return new NhanVien(
                            rs.getInt("id"),
                            rs.getString("tai_khoan"),
                            rs.getString("mat_khau"),
                            rs.getString("ho_ten"),
                            rs.getString("vai_tro"),
                            rs.getInt("trang_thai")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi checkLogin: " + e.getMessage());
        }
        return null; // Đăng nhập thất bại
    }

    public List<NhanVien> findAll() {
        List<NhanVien> list = new ArrayList<>();
        String sql = "SELECT * FROM nhan_vien";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                NhanVien nv = new NhanVien(
                        rs.getInt("id"),
                        rs.getString("tai_khoan"),
                        rs.getString("mat_khau"),
                        rs.getString("ho_ten"),
                        rs.getString("vai_tro"),
                        rs.getInt("trang_thai")
                );
                list.add(nv);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findAll NhanVien: " + e.getMessage());
        }
        return list;
    }

    public boolean insert(NhanVien nv) {
        String sql = "INSERT INTO nhan_vien (tai_khoan, mat_khau, ho_ten, vai_tro, trang_thai) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setString(1, nv.getTaiKhoan());
            pstm.setString(2, nv.getMatKhau());
            pstm.setString(3, nv.getHoTen());
            pstm.setString(4, nv.getVaiTro());
            pstm.setInt(5, nv.getTrangThai());

            return pstm.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi insert NhanVien: " + e.getMessage());
            return false;
        }
    }

    public boolean update(NhanVien nv) {
        String sql = "UPDATE nhan_vien SET mat_khau = ?, ho_ten = ?, vai_tro = ?, trang_thai = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setString(1, nv.getMatKhau());
            pstm.setString(2, nv.getHoTen());
            pstm.setString(3, nv.getVaiTro());
            pstm.setInt(4, nv.getTrangThai());
            pstm.setInt(5, nv.getId());

            return pstm.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi update NhanVien: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "UPDATE nhan_vien SET trang_thai = 0 WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setInt(1, id);
            return pstm.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi delete NhanVien: " + e.getMessage());
            return false;
        }
    }
}
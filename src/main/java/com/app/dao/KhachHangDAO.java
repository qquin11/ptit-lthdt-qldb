package com.app.dao;

import com.app.model.KhachHang;
import com.app.service.DataAccessException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class KhachHangDAO extends BaseDAO {

    public List<KhachHang> findAll() {
        try (PreparedStatement ps = getConnection().prepareStatement("SELECT * FROM khach_hang ORDER BY ho_ten");
             ResultSet rs = ps.executeQuery()) {
            List<KhachHang> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (Exception e) {
            throw new DataAccessException("findAll KhachHang", e);
        }
    }

    public KhachHang findBySdt(String sdt) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM khach_hang WHERE so_dien_thoai = ?")) {
            ps.setString(1, sdt);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) {
            throw new DataAccessException("findBySdt", e);
        }
    }

    public int insert(KhachHang k) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "INSERT INTO khach_hang (ho_ten, so_dien_thoai, diem_tich_luy) VALUES (?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, k.getHoTen());
            ps.setString(2, k.getSoDienThoai());
            ps.setInt(3, k.getDiemTichLuy());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        } catch (Exception e) {
            throw new DataAccessException("insert KhachHang", e);
        }
    }

    private KhachHang map(ResultSet rs) throws java.sql.SQLException {
        return new KhachHang(
                rs.getInt("id"),
                rs.getString("ho_ten"),
                rs.getString("so_dien_thoai"),
                rs.getInt("diem_tich_luy"));
    }
}

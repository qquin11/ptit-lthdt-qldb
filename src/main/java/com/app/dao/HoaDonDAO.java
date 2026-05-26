package com.app.dao;

import com.app.model.HoaDon;
import com.app.service.DataAccessException;
import com.app.util.DateFormatter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HoaDonDAO extends BaseDAO {

    public HoaDon findById(int id) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM hoa_don WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) {
            throw new DataAccessException("findById HoaDon", e);
        }
    }

    /** Hóa đơn chưa thanh toán cho bàn — dùng để load draft khi mở Order */
    public HoaDon findOpenByBan(int banId) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM hoa_don WHERE ban_an_id = ? AND trang_thai = 0 ORDER BY id DESC LIMIT 1")) {
            ps.setInt(1, banId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) {
            throw new DataAccessException("findOpenByBan", e);
        }
    }

    public int insert(HoaDon h) {
        String sql = "INSERT INTO hoa_don " +
                "(ban_an_id, nhan_vien_id, khach_hang_id, ngay_tao, tong_tien, vat, giam_gia, " +
                "trang_thai, tien_khach_dua, phuong_thuc_tt) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, h.getBanAnId());
            ps.setInt(2, h.getNhanVienId());
            if (h.getKhachHangId() == null) ps.setNull(3, Types.INTEGER);
            else ps.setInt(3, h.getKhachHangId());
            ps.setString(4, h.getNgayTao());
            ps.setDouble(5, h.getTongTien());
            ps.setDouble(6, h.getVat());
            ps.setDouble(7, h.getGiamGia());
            ps.setInt(8, h.getTrangThai());
            ps.setDouble(9, h.getTienKhachDua());
            ps.setString(10, h.getPhuongThucTt() == null ? HoaDon.PT_TIEN_MAT : h.getPhuongThucTt());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        } catch (Exception e) {
            throw new DataAccessException("insert HoaDon", e);
        }
    }

    public boolean update(HoaDon h) {
        String sql = "UPDATE hoa_don SET tong_tien=?, vat=?, giam_gia=?, trang_thai=?, " +
                "tien_khach_dua=?, phuong_thuc_tt=?, khach_hang_id=? WHERE id=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setDouble(1, h.getTongTien());
            ps.setDouble(2, h.getVat());
            ps.setDouble(3, h.getGiamGia());
            ps.setInt(4, h.getTrangThai());
            ps.setDouble(5, h.getTienKhachDua());
            ps.setString(6, h.getPhuongThucTt() == null ? HoaDon.PT_TIEN_MAT : h.getPhuongThucTt());
            if (h.getKhachHangId() == null) ps.setNull(7, Types.INTEGER);
            else ps.setInt(7, h.getKhachHangId());
            ps.setInt(8, h.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException("update HoaDon", e);
        }
    }

    /** Doanh thu hôm nay (chỉ tính HĐ đã thanh toán). */
    public double sumTodayRevenue() {
        String today = DateFormatter.formatDate(LocalDate.now())
                .replaceAll("/", "-"); // dd-MM-yyyy
        // Đơn giản: dùng DATE() function sqlite trên ngay_tao (yyyy-MM-dd HH:mm:ss)
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT COALESCE(SUM(tong_tien * (1 + vat) - giam_gia), 0) " +
                "FROM hoa_don WHERE trang_thai = 1 AND DATE(ngay_tao) = DATE('now', 'localtime')");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        } catch (Exception e) {
            throw new DataAccessException("sumTodayRevenue", e);
        }
    }

    public int countToday() {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT COUNT(*) FROM hoa_don WHERE DATE(ngay_tao) = DATE('now', 'localtime')");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            throw new DataAccessException("countToday HoaDon", e);
        }
    }

    public List<HoaDon> findRecent(int limit) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM hoa_don ORDER BY id DESC LIMIT ?")) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<HoaDon> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (Exception e) {
            throw new DataAccessException("findRecent HoaDon", e);
        }
    }

    private HoaDon map(ResultSet rs) throws java.sql.SQLException {
        HoaDon h = new HoaDon(
                rs.getInt("id"),
                rs.getInt("ban_an_id"),
                rs.getInt("nhan_vien_id"),
                (Integer) rs.getObject("khach_hang_id"),
                rs.getString("ngay_tao"),
                rs.getDouble("tong_tien"),
                rs.getDouble("vat"),
                rs.getDouble("giam_gia"),
                rs.getInt("trang_thai"));
        // Migration cột (có thể null nếu old row)
        try { h.setTienKhachDua(rs.getDouble("tien_khach_dua")); } catch (Exception ignored) {}
        try { h.setPhuongThucTt(rs.getString("phuong_thuc_tt")); } catch (Exception ignored) {}
        return h;
    }
}

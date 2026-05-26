package com.app.dao;

import com.app.model.DatBan;
import com.app.service.DataAccessException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatBanDAO extends BaseDAO {

    public int insert(DatBan d) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "INSERT INTO dat_ban (khach_hang_id, ban_an_id, thoi_gian_dat, ghi_chu, trang_thai) " +
                        "VALUES (?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, d.getKhachHangId());
            ps.setInt(2, d.getBanAnId());
            ps.setString(3, d.getThoiGianDat());
            ps.setString(4, d.getGhiChu());
            ps.setString(5, d.getTrangThai() == null ? "CHO_DEN" : d.getTrangThai());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        } catch (Exception e) {
            throw new DataAccessException("insert DatBan", e);
        }
    }

    public List<DatBan> findAll() {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM dat_ban ORDER BY thoi_gian_dat DESC");
             ResultSet rs = ps.executeQuery()) {
            List<DatBan> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (Exception e) {
            throw new DataAccessException("findAll DatBan", e);
        }
    }

    /**
     * Check conflict: trả về bàn nào đang bận trong khoảng ±slotHours quanh thời điểm cho.
     * Dùng cho Reservation screen kiểm tra trống.
     */
    public List<Integer> findBusyTableIds(String dateTimeSql, int slotHours) {
        String sql = """
            SELECT DISTINCT ban_an_id FROM dat_ban
            WHERE trang_thai != 'HUY'
              AND ABS(strftime('%s', thoi_gian_dat) - strftime('%s', ?)) < ? * 3600
            """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, dateTimeSql);
            ps.setInt(2, slotHours);
            try (ResultSet rs = ps.executeQuery()) {
                List<Integer> ids = new ArrayList<>();
                while (rs.next()) ids.add(rs.getInt(1));
                return ids;
            }
        } catch (Exception e) {
            throw new DataAccessException("findBusyTableIds", e);
        }
    }

    public boolean updateTrangThai(int id, String trangThai) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "UPDATE dat_ban SET trang_thai = ? WHERE id = ?")) {
            ps.setString(1, trangThai);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException("updateTrangThai DatBan", e);
        }
    }

    private DatBan map(ResultSet rs) throws java.sql.SQLException {
        return new DatBan(
                rs.getInt("id"),
                rs.getInt("khach_hang_id"),
                rs.getInt("ban_an_id"),
                rs.getString("thoi_gian_dat"),
                rs.getString("ghi_chu"),
                rs.getString("trang_thai"));
    }
}

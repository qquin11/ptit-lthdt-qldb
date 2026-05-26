package com.app.dao;

import com.app.model.MonAn;
import com.app.service.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MonAnDAO extends BaseDAO {

    public List<MonAn> findAll() {
        return query("SELECT * FROM mon_an ORDER BY id");
    }

    public List<MonAn> findByDanhMuc(int danhMucId) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM mon_an WHERE danh_muc_id = ? ORDER BY ten_mon")) {
            ps.setInt(1, danhMucId);
            try (ResultSet rs = ps.executeQuery()) {
                List<MonAn> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (Exception e) {
            throw new DataAccessException("findByDanhMuc lỗi", e);
        }
    }

    public List<MonAn> findAvailable() {
        return query("SELECT * FROM mon_an WHERE con_hang = 1 ORDER BY ten_mon");
    }

    public MonAn findById(int id) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM mon_an WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) {
            throw new DataAccessException("findById MonAn", e);
        }
    }

    public int insert(MonAn m) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "INSERT INTO mon_an (ten_mon, gia_tien, danh_muc_id, con_hang) VALUES (?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getTenMon());
            ps.setDouble(2, m.getGiaTien());
            ps.setInt(3, m.getDanhMucId());
            ps.setInt(4, m.getConHang());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        } catch (Exception e) {
            throw new DataAccessException("insert MonAn", e);
        }
    }

    public boolean update(MonAn m) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "UPDATE mon_an SET ten_mon=?, gia_tien=?, danh_muc_id=?, con_hang=? WHERE id=?")) {
            ps.setString(1, m.getTenMon());
            ps.setDouble(2, m.getGiaTien());
            ps.setInt(3, m.getDanhMucId());
            ps.setInt(4, m.getConHang());
            ps.setInt(5, m.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException("update MonAn", e);
        }
    }

    public boolean delete(int id) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "DELETE FROM mon_an WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException("delete MonAn", e);
        }
    }

    private List<MonAn> query(String sql) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<MonAn> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (Exception e) {
            throw new DataAccessException("query MonAn: " + sql, e);
        }
    }

    private MonAn map(ResultSet rs) throws java.sql.SQLException {
        return new MonAn(
                rs.getInt("id"),
                rs.getString("ten_mon"),
                rs.getDouble("gia_tien"),
                rs.getInt("danh_muc_id"),
                rs.getInt("con_hang"));
    }
}

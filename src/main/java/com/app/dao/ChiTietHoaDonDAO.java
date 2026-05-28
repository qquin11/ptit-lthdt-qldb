package com.app.dao;

import com.app.model.ChiTietHoaDon;
import com.app.service.DataAccessException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ChiTietHoaDonDAO extends BaseDAO {

    public List<ChiTietHoaDon> findByHoaDon(int hoaDonId) {
        String sql = """
            SELECT cth.*, ma.ten_mon
            FROM chi_tiet_hoa_don cth
            JOIN mon_an ma ON ma.id = cth.mon_an_id
            WHERE cth.hoa_don_id = ?
            """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, hoaDonId);
            try (ResultSet rs = ps.executeQuery()) {
                List<ChiTietHoaDon> list = new ArrayList<>();
                while (rs.next()) {
                    ChiTietHoaDon c = new ChiTietHoaDon(
                            rs.getInt("hoa_don_id"),
                            rs.getInt("mon_an_id"),
                            rs.getInt("so_luong"),
                            rs.getDouble("gia_luc_mua"),
                            rs.getString("ghi_chu"));
                    c.setTenMon(rs.getString("ten_mon"));
                    list.add(c);
                }
                return list;
            }
        } catch (Exception e) {
            throw new DataAccessException("findByHoaDon", e);
        }
    }

    /** Upsert: tăng SL nếu món đã có, insert nếu chưa. */
    public void upsert(ChiTietHoaDon c) {
        String sql = """
            INSERT INTO chi_tiet_hoa_don (hoa_don_id, mon_an_id, so_luong, gia_luc_mua, ghi_chu)
            VALUES (?,?,?,?,?)
            ON CONFLICT(hoa_don_id, mon_an_id) DO UPDATE SET
                so_luong = so_luong + excluded.so_luong,
                gia_luc_mua = excluded.gia_luc_mua,
                ghi_chu = excluded.ghi_chu
            """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, c.getHoaDonId());
            ps.setInt(2, c.getMonAnId());
            ps.setInt(3, c.getSoLuong());
            ps.setDouble(4, c.getGiaLucMua());
            ps.setString(5, c.getGhiChu());
            ps.executeUpdate();
        } catch (Exception e) {
            throw new DataAccessException("upsert ChiTietHoaDon", e);
        }
    }

    public void setQuantity(int hoaDonId, int monAnId, int quantity) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "UPDATE chi_tiet_hoa_don SET so_luong = ? WHERE hoa_don_id = ? AND mon_an_id = ?")) {
            ps.setInt(1, quantity);
            ps.setInt(2, hoaDonId);
            ps.setInt(3, monAnId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new DataAccessException("setQuantity", e);
        }
    }

    public void remove(int hoaDonId, int monAnId) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "DELETE FROM chi_tiet_hoa_don WHERE hoa_don_id = ? AND mon_an_id = ?")) {
            ps.setInt(1, hoaDonId);
            ps.setInt(2, monAnId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new DataAccessException("remove ChiTietHoaDon", e);
        }
    }

    public void deleteByHoaDon(int hoaDonId) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "DELETE FROM chi_tiet_hoa_don WHERE hoa_don_id = ?")) {
            ps.setInt(1, hoaDonId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new DataAccessException("deleteByHoaDon", e);
        }
    }
}

package com.app.dao;

import com.app.model.BanAn;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BanAnDAO extends BaseDAO {

    public List<BanAn> findAll() {
        List<BanAn> list = new ArrayList<>();
        String sql = "SELECT * FROM ban_an";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                BanAn ban = new BanAn(
                        rs.getInt("id"),
                        rs.getString("ten_ban"),
                        rs.getString("trang_thai")
                );
                list.add(ban);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findAll BanAn: " + e.getMessage());
        }
        return list;
    }
    // trạng thái truyền vào: "TRONG", "DANG_DUNG", "DA_DAT"
    public boolean updateTrangThai(int banId, String trangThaiMoi) {
        String sql = "UPDATE ban_an SET trang_thai = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setString(1, trangThaiMoi);
            pstm.setInt(2, banId);

            return pstm.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi updateTrangThai BanAn: " + e.getMessage());
            return false;
        }
    }

    public boolean insert(BanAn ban) {
        String sql = "INSERT INTO ban_an (ten_ban, trang_thai) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setString(1, ban.getTenBan());
            pstm.setString(2, ban.getTrangThai());

            return pstm.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi insert BanAn: " + e.getMessage());
            return false;
        }
    }
}
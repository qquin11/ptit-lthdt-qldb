package com.app.dao;

import com.app.model.DanhMuc;
import com.app.service.DataAccessException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DanhMucDAO extends BaseDAO {

    public List<DanhMuc> findAll() {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM danh_muc ORDER BY ten_danh_muc");
             ResultSet rs = ps.executeQuery()) {
            List<DanhMuc> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new DanhMuc(rs.getInt("id"), rs.getString("ten_danh_muc")));
            }
            return list;
        } catch (Exception e) {
            throw new DataAccessException("findAll DanhMuc", e);
        }
    }

    public int insert(DanhMuc d) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "INSERT INTO danh_muc (ten_danh_muc) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getTenDanhMuc());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        } catch (Exception e) {
            throw new DataAccessException("insert DanhMuc", e);
        }
    }
}

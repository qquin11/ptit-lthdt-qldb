package com.app.service;

import com.app.dao.DanhMucDAO;
import com.app.dao.MonAnDAO;
import com.app.model.DanhMuc;
import com.app.model.MonAn;
import com.app.util.ValidationUtils;

import java.util.List;

/** CRUD món ăn + danh mục. */
public class MenuService {

    private final MonAnDAO monAnDAO = new MonAnDAO();
    private final DanhMucDAO danhMucDAO = new DanhMucDAO();

    public List<DanhMuc> listCategories() { return danhMucDAO.findAll(); }
    public List<MonAn> listAll()          { return monAnDAO.findAll(); }
    public List<MonAn> listAvailable()    { return monAnDAO.findAvailable(); }
    public List<MonAn> listByCategory(int dmId) { return monAnDAO.findByDanhMuc(dmId); }
    public MonAn findById(int id)         { return monAnDAO.findById(id); }

    public MonAn create(MonAn m) {
        validate(m);
        int id = monAnDAO.insert(m);
        m.setId(id);
        return m;
    }

    public boolean update(MonAn m) {
        validate(m);
        return monAnDAO.update(m);
    }

    public boolean delete(int id) { return monAnDAO.delete(id); }

    public DanhMuc createCategory(String ten) {
        if (!ValidationUtils.isRequired(ten)) throw new DataAccessException("Tên danh mục bắt buộc");
        DanhMuc d = new DanhMuc(0, ten.trim());
        int id = danhMucDAO.insert(d);
        d.setId(id);
        return d;
    }

    private void validate(MonAn m) {
        if (m == null) throw new DataAccessException("Món không hợp lệ");
        if (!ValidationUtils.isRequired(m.getTenMon())) throw new DataAccessException("Tên món bắt buộc");
        if (!ValidationUtils.isPositive(m.getGiaTien())) throw new DataAccessException("Giá phải > 0");
        if (m.getDanhMucId() <= 0) throw new DataAccessException("Phải chọn danh mục");
    }
}

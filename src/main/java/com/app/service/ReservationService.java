package com.app.service;

import com.app.config.AppConfig;
import com.app.dao.DatBanDAO;
import com.app.dao.KhachHangDAO;
import com.app.model.DatBan;
import com.app.model.KhachHang;
import com.app.util.DateFormatter;
import com.app.util.ValidationUtils;

import java.time.LocalDateTime;
import java.util.List;

/** Tạo & quản lý đặt bàn — auto tạo khách nếu SĐT mới, check conflict. */
public class ReservationService {

    private final DatBanDAO datBanDAO = new DatBanDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();

    /**
     * Tạo booking. Validate input + conflict bàn ±2h.
     * @return DatBan với ID đã set.
     */
    public DatBan create(String hoTen, String sdt, int banId, LocalDateTime thoiGian, String ghiChu) {
        if (!ValidationUtils.isRequired(hoTen)) throw new DataAccessException("Tên khách bắt buộc");
        if (!ValidationUtils.isPhoneVN(sdt))   throw new DataAccessException("SĐT không hợp lệ (10 số bắt đầu 0)");
        if (thoiGian == null || thoiGian.isBefore(LocalDateTime.now()))
            throw new DataAccessException("Thời gian đặt phải ở tương lai");

        String sqlTime = DateFormatter.toSql(thoiGian);

        // Conflict check
        List<Integer> busy = datBanDAO.findBusyTableIds(sqlTime, AppConfig.RESERVATION_SLOT_HOURS);
        if (busy.contains(banId)) {
            throw new DataAccessException("Bàn đã có đặt khác trong khoảng thời gian này");
        }

        // Find or create khách
        KhachHang kh = khachHangDAO.findBySdt(sdt);
        if (kh == null) {
            kh = new KhachHang(0, hoTen, sdt, 0);
            int id = khachHangDAO.insert(kh);
            kh.setId(id);
        }

        DatBan d = new DatBan(0, kh.getId(), banId, sqlTime, ghiChu, "CHO_DEN");
        int id = datBanDAO.insert(d);
        d.setId(id);
        return d;
    }

    public List<Integer> getBusyTableIds(LocalDateTime at) {
        return datBanDAO.findBusyTableIds(DateFormatter.toSql(at), AppConfig.RESERVATION_SLOT_HOURS);
    }

    public List<DatBan> listAll() { return datBanDAO.findAll(); }

    public boolean cancel(int datBanId) { return datBanDAO.updateTrangThai(datBanId, "HUY"); }
    public boolean checkin(int datBanId) { return datBanDAO.updateTrangThai(datBanId, "DA_NHAN"); }
}

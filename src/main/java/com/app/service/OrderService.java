package com.app.service;

import com.app.config.AppConfig;
import com.app.dao.ChiTietHoaDonDAO;
import com.app.dao.HoaDonDAO;
import com.app.model.ChiTietHoaDon;
import com.app.model.HoaDon;
import com.app.model.MonAn;
import com.app.util.DateFormatter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Order = HoaDon (status=0 chưa thanh toán) + ChiTietHoaDon (line items).
 * Lưu draft khi user add món, finalize khi thanh toán.
 */
public class OrderService {

    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final ChiTietHoaDonDAO chiTietDAO = new ChiTietHoaDonDAO();
    private final TableService tableService = new TableService();

    /** Tải hoặc tạo order chưa thanh toán cho bàn (idempotent). */
    public HoaDon loadOrCreateDraft(int banId, int nhanVienId) {
        HoaDon existing = hoaDonDAO.findOpenByBan(banId);
        if (existing != null) return existing;

        HoaDon h = new HoaDon();
        h.setBanAnId(banId);
        h.setNhanVienId(nhanVienId);
        h.setNgayTao(DateFormatter.toSql(LocalDateTime.now()));
        h.setVat(AppConfig.DEFAULT_VAT_RATE);
        h.setTongTien(0);
        h.setTrangThai(0);
        h.setPhuongThucTt(HoaDon.PT_TIEN_MAT);
        int id = hoaDonDAO.insert(h);
        h.setId(id);

        // Đánh dấu bàn đang dùng
        tableService.markOccupied(banId);
        return h;
    }

    public List<ChiTietHoaDon> getItems(int hoaDonId) {
        return chiTietDAO.findByHoaDon(hoaDonId);
    }

    public void addItem(int hoaDonId, MonAn mon, int qty, String note) {
        ChiTietHoaDon c = new ChiTietHoaDon(hoaDonId, mon.getId(), qty, mon.getGiaTien(), note);
        chiTietDAO.upsert(c);
        recalcTotal(hoaDonId);
    }

    public void setQuantity(int hoaDonId, int monAnId, int qty) {
        if (qty <= 0) {
            chiTietDAO.remove(hoaDonId, monAnId);
        } else {
            chiTietDAO.setQuantity(hoaDonId, monAnId, qty);
        }
        recalcTotal(hoaDonId);
    }

    public void removeItem(int hoaDonId, int monAnId) {
        chiTietDAO.remove(hoaDonId, monAnId);
        recalcTotal(hoaDonId);
    }

    public double recalcTotal(int hoaDonId) {
        List<ChiTietHoaDon> items = chiTietDAO.findByHoaDon(hoaDonId);
        double total = items.stream().mapToDouble(ChiTietHoaDon::getThanhTien).sum();
        HoaDon target = hoaDonDAO.findById(hoaDonId);
        if (target != null) {
            target.setTongTien(total);
            hoaDonDAO.update(target);
        }
        return total;
    }
}

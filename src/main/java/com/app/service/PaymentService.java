package com.app.service;

import com.app.dao.HoaDonDAO;
import com.app.model.HoaDon;

/**
 * Finalize HĐ + release bàn. Validate tiền khách đưa >= tổng cuối.
 */
public class PaymentService {

    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final TableService tableService = new TableService();

    public HoaDon finalize(HoaDon hd, double tienKhachDua, double giamGia, String phuongThuc) {
        if (hd == null || hd.getId() == 0) {
            throw new DataAccessException("Hóa đơn không hợp lệ");
        }
        hd.setGiamGia(giamGia);
        if (tienKhachDua < hd.getThanhToanCuoi()) {
            throw new DataAccessException("Tiền khách đưa không đủ để thanh toán");
        }
        hd.setTienKhachDua(tienKhachDua);
        hd.setPhuongThucTt(phuongThuc == null ? HoaDon.PT_TIEN_MAT : phuongThuc);
        hd.setTrangThai(1);

        boolean ok = hoaDonDAO.update(hd);
        if (!ok) throw new DataAccessException("Lưu hóa đơn thất bại");

        tableService.release(hd.getBanAnId());
        return hd;
    }

    public double sumTodayRevenue()  { return hoaDonDAO.sumTodayRevenue(); }
    public int    countToday()       { return hoaDonDAO.countToday(); }
}

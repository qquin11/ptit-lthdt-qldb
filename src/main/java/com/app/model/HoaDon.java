package com.app.model;

public class HoaDon {
    public static final String PT_TIEN_MAT     = "TIEN_MAT";
    public static final String PT_CHUYEN_KHOAN = "CHUYEN_KHOAN";
    public static final String PT_THE          = "THE";

    private int id;
    private int banAnId;
    private int nhanVienId;
    private Integer khachHangId; // NULLable
    private String ngayTao;      // YYYY-MM-DD HH:MM:SS
    private double tongTien;
    private double vat;          // Mặc định 0.1
    private double giamGia;
    private int trangThai;       // 0: Chưa thanh toán, 1: Đã thanh toán
    private double tienKhachDua; // Phase 04 migration
    private String phuongThucTt; // Phase 04 migration (TIEN_MAT/CHUYEN_KHOAN/THE)

    public HoaDon() {}

    public HoaDon(int id, int banAnId, int nhanVienId, Integer khachHangId, String ngayTao, double tongTien, double vat, double giamGia, int trangThai) {
        this.id = id;
        this.banAnId = banAnId;
        this.nhanVienId = nhanVienId;
        this.khachHangId = khachHangId;
        this.ngayTao = ngayTao;
        this.tongTien = tongTien;
        this.vat = vat;
        this.giamGia = giamGia;
        this.trangThai = trangThai;
        this.phuongThucTt = PT_TIEN_MAT;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBanAnId() { return banAnId; }
    public void setBanAnId(int banAnId) { this.banAnId = banAnId; }

    public int getNhanVienId() { return nhanVienId; }
    public void setNhanVienId(int nhanVienId) { this.nhanVienId = nhanVienId; }

    public Integer getKhachHangId() { return khachHangId; }
    public void setKhachHangId(Integer khachHangId) { this.khachHangId = khachHangId; }

    public String getNgayTao() { return ngayTao; }
    public void setNgayTao(String ngayTao) { this.ngayTao = ngayTao; }

    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }

    public double getVat() { return vat; }
    public void setVat(double vat) { this.vat = vat; }

    public double getGiamGia() { return giamGia; }
    public void setGiamGia(double giamGia) { this.giamGia = giamGia; }

    public int getTrangThai() { return trangThai; }
    public void setTrangThai(int trangThai) { this.trangThai = trangThai; }

    public double getTienKhachDua() { return tienKhachDua; }
    public void setTienKhachDua(double tienKhachDua) { this.tienKhachDua = tienKhachDua; }

    public String getPhuongThucTt() { return phuongThucTt; }
    public void setPhuongThucTt(String phuongThucTt) { this.phuongThucTt = phuongThucTt; }

    /** Tổng sau VAT - giảm giá */
    public double getThanhToanCuoi() {
        return tongTien * (1 + vat) - giamGia;
    }

    /** Tiền thừa = tien khách đưa - tổng cuối */
    public double getTienThua() {
        return tienKhachDua - getThanhToanCuoi();
    }
}
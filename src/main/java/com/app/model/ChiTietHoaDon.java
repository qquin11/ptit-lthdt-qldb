package com.app.model;

/**
 * Line item của hóa đơn — chi tiết từng món + số lượng + giá lúc mua.
 * Map với bảng {@code chi_tiet_hoa_don} (composite PK: hoa_don_id + mon_an_id).
 */
public class ChiTietHoaDon {
    private int hoaDonId;
    private int monAnId;
    private int soLuong;
    private double giaLucMua;
    private String ghiChu;

    // Bonus: tên món cho UI (join query)
    private String tenMon;

    public ChiTietHoaDon() {}

    public ChiTietHoaDon(int hoaDonId, int monAnId, int soLuong, double giaLucMua, String ghiChu) {
        this.hoaDonId = hoaDonId;
        this.monAnId = monAnId;
        this.soLuong = soLuong;
        this.giaLucMua = giaLucMua;
        this.ghiChu = ghiChu;
    }

    public double getThanhTien() { return soLuong * giaLucMua; }

    public int getHoaDonId() { return hoaDonId; }
    public void setHoaDonId(int hoaDonId) { this.hoaDonId = hoaDonId; }
    public int getMonAnId() { return monAnId; }
    public void setMonAnId(int monAnId) { this.monAnId = monAnId; }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
    public double getGiaLucMua() { return giaLucMua; }
    public void setGiaLucMua(double giaLucMua) { this.giaLucMua = giaLucMua; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public String getTenMon() { return tenMon; }
    public void setTenMon(String tenMon) { this.tenMon = tenMon; }
}

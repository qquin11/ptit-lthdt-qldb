package com.app.model;

public class KhachHang {
    private int id;
    private String hoTen;
    private String soDienThoai;
    private int diemTichLuy;

    public KhachHang() {}

    public KhachHang(int id, String hoTen, String soDienThoai, int diemTichLuy) {
        this.id = id;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.diemTichLuy = diemTichLuy;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    public int getDiemTichLuy() { return diemTichLuy; }
    public void setDiemTichLuy(int diemTichLuy) { this.diemTichLuy = diemTichLuy; }
}
package com.app.model;

public class MonAn {
    private int id;
    private String tenMon;
    private double giaTien;
    private int danhMucId; // Khóa ngoại liên kết bảng DanhMuc
    private int conHang;   // 1: Còn hàng, 0: Hết hàng

    public MonAn() {}

    public MonAn(int id, String tenMon, double giaTien, int danhMucId, int conHang) {
        this.id = id;
        this.tenMon = tenMon;
        this.giaTien = giaTien;
        this.danhMucId = danhMucId;
        this.conHang = conHang;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTenMon() { return tenMon; }
    public void setTenMon(String tenMon) { this.tenMon = tenMon; }

    public double getGiaTien() { return giaTien; }
    public void setGiaTien(double giaTien) { this.giaTien = giaTien; }

    public int getDanhMucId() { return danhMucId; }
    public void setDanhMucId(int danhMucId) { this.danhMucId = danhMucId; }

    public int getConHang() { return conHang; }
    public void setConHang(int conHang) { this.conHang = conHang; }
}
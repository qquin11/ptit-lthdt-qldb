package com.app.model;

public class DatBan {
    private int id;
    private int khachHangId;
    private int banAnId;
    private String thoiGianDat; //YYYY-MM-DD HH:MM:SS
    private String ghiChu;
    private String trangThai;   // CHO_DEN, DA_NHAN, HUY

    public DatBan() {}

    public DatBan(int id, int khachHangId, int banAnId, String thoiGianDat, String ghiChu, String trangThai) {
        this.id = id;
        this.khachHangId = khachHangId;
        this.banAnId = banAnId;
        this.thoiGianDat = thoiGianDat;
        this.ghiChu = ghiChu;
        this.trangThai = trangThai;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getKhachHangId() { return khachHangId; }
    public void setKhachHangId(int khachHangId) { this.khachHangId = khachHangId; }

    public int getBanAnId() { return banAnId; }
    public void setBanAnId(int banAnId) { this.banAnId = banAnId; }

    public String getThoiGianDat() { return thoiGianDat; }
    public void setThoiGianDat(String thoiGianDat) { this.thoiGianDat = thoiGianDat; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
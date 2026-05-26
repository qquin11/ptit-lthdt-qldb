package com.app.model;

public class DanhMuc {
    private int id;
    private String tenDanhMuc;

    public DanhMuc() {}

    public DanhMuc(int id, String tenDanhMuc) {
        this.id = id;
        this.tenDanhMuc = tenDanhMuc;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTenDanhMuc() { return tenDanhMuc; }
    public void setTenDanhMuc(String tenDanhMuc) { this.tenDanhMuc = tenDanhMuc; }
}
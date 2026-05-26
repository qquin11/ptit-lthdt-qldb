package com.app.service;

import com.app.dao.BanAnDAO;
import com.app.model.BanAn;

import java.util.List;

/** Business logic cho bàn ăn: list, đổi status. */
public class TableService {

    public static final String TRONG = "TRONG";
    public static final String DANG_DUNG = "DANG_DUNG";
    public static final String DA_DAT = "DA_DAT";

    private final BanAnDAO banAnDAO = new BanAnDAO();

    public List<BanAn> listAll() {
        return banAnDAO.findAll();
    }

    public boolean markOccupied(int banId) {
        return banAnDAO.updateTrangThai(banId, DANG_DUNG);
    }

    public boolean markReserved(int banId) {
        return banAnDAO.updateTrangThai(banId, DA_DAT);
    }

    public boolean release(int banId) {
        return banAnDAO.updateTrangThai(banId, TRONG);
    }
}

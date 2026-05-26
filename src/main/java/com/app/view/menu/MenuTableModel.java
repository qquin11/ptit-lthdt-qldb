package com.app.view.menu;

import com.app.model.DanhMuc;
import com.app.model.MonAn;
import com.app.util.CurrencyFormatter;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Table model cho Menu — joins MonAn với tên DanhMuc cho display. */
public class MenuTableModel extends AbstractTableModel {

    private final String[] cols = {"Mã", "Tên món", "Danh mục", "Giá", "Tình trạng"};
    private List<MonAn> data = new ArrayList<>();
    private Map<Integer, String> categoryNames = new HashMap<>();

    public void setData(List<MonAn> items, List<DanhMuc> categories) {
        this.data = items == null ? new ArrayList<>() : items;
        this.categoryNames = new HashMap<>();
        if (categories != null) {
            for (DanhMuc d : categories) categoryNames.put(d.getId(), d.getTenDanhMuc());
        }
        fireTableDataChanged();
    }

    public MonAn getRow(int i) { return data.get(i); }

    @Override public int getRowCount() { return data.size(); }
    @Override public int getColumnCount() { return cols.length; }
    @Override public String getColumnName(int c) { return cols[c]; }

    @Override
    public Object getValueAt(int r, int c) {
        MonAn m = data.get(r);
        return switch (c) {
            case 0 -> m.getId();
            case 1 -> m.getTenMon();
            case 2 -> categoryNames.getOrDefault(m.getDanhMucId(), "(--)");
            case 3 -> CurrencyFormatter.format(m.getGiaTien());
            case 4 -> m.getConHang() == 1 ? "Đang bán" : "Hết hàng";
            default -> "";
        };
    }
}

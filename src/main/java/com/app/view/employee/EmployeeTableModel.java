package com.app.view.employee;

import com.app.model.NhanVien;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/** Table model cho danh sách nhân viên. */
public class EmployeeTableModel extends AbstractTableModel {

    private final String[] cols = {"Mã", "Tài khoản", "Họ tên", "Vai trò", "Tình trạng"};
    private List<NhanVien> data = new ArrayList<>();

    public void setData(List<NhanVien> items) {
        this.data = items == null ? new ArrayList<>() : items;
        fireTableDataChanged();
    }

    public NhanVien getRow(int i) { return data.get(i); }

    @Override public int getRowCount() { return data.size(); }
    @Override public int getColumnCount() { return cols.length; }
    @Override public String getColumnName(int c) { return cols[c]; }

    @Override
    public Object getValueAt(int r, int c) {
        NhanVien n = data.get(r);
        return switch (c) {
            case 0 -> n.getId();
            case 1 -> n.getTaiKhoan();
            case 2 -> n.getHoTen();
            case 3 -> "ADMIN".equals(n.getVaiTro()) ? "Quản lý" : "Nhân viên";
            case 4 -> n.getTrangThai() == 1 ? "Đang làm" : "Nghỉ việc";
            default -> "";
        };
    }
}

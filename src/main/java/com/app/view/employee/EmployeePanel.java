package com.app.view.employee;

import com.app.config.AppFonts;
import com.app.config.AppSpacing;
import com.app.model.NhanVien;
import com.app.service.EmployeeService;
import com.app.util.SwingWorkerHelper;
import com.app.view.common.ConfirmDialog;
import com.app.view.common.GhostButton;
import com.app.view.common.Pagination;
import com.app.view.common.PrimaryButton;
import com.app.view.common.SearchField;
import com.app.view.common.Toast;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;

/** CRUD nhân viên — JTable + filter + form dialog + reset password. */
public class EmployeePanel extends JPanel {

    private final EmployeeService employeeService = new EmployeeService();
    private final EmployeeTableModel model = new EmployeeTableModel();
    private final JTable table = new JTable(model);
    private final SearchField search = new SearchField("Tìm nhân viên...");
    private final Pagination pagination = new Pagination(15);
    private java.util.List<NhanVien> allItems = new java.util.ArrayList<>();

    public EmployeePanel() {
        super(new BorderLayout(0, AppSpacing.MD));
        setBorder(BorderFactory.createEmptyBorder(AppSpacing.LG, AppSpacing.LG, AppSpacing.LG, AppSpacing.LG));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(),  BorderLayout.CENTER);
        add(pagination,    BorderLayout.SOUTH);

        search.onTextChanged(s -> applyFilter());
        pagination.onPageChange(p -> renderPage());
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, AppSpacing.SM));
        header.setOpaque(false);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        JLabel title = new JLabel("Quản lý Nhân viên");
        title.setFont(AppFonts.h1 == null ? title.getFont() : AppFonts.h1);
        titleRow.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppSpacing.SM, 0));
        actions.setOpaque(false);
        PrimaryButton btnAdd = new PrimaryButton("+ Thêm nhân viên");
        btnAdd.addActionListener(e -> openForm(null));
        GhostButton btnReload = new GhostButton("↻ Reload");
        btnReload.addActionListener(e -> refresh());
        actions.add(btnReload);
        actions.add(btnAdd);
        titleRow.add(actions, BorderLayout.EAST);

        header.add(titleRow, BorderLayout.NORTH);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, AppSpacing.SM, 0));
        filters.setOpaque(false);
        filters.add(search);
        header.add(filters, BorderLayout.CENTER);
        return header;
    }

    private JScrollPane buildTable() {
        table.setRowHeight(36);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    openForm(model.getRow(table.getSelectedRow()));
                }
                if (javax.swing.SwingUtilities.isRightMouseButton(e)) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        table.setRowSelectionInterval(row, row);
                        showContextMenu(e.getX(), e.getY());
                    }
                }
            }
        });

        return new JScrollPane(table);
    }

    private void showContextMenu(int x, int y) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem mEdit = new JMenuItem("Sửa");
        JMenuItem mReset = new JMenuItem("Đặt lại mật khẩu");
        JMenuItem mDeactivate = new JMenuItem("Nghỉ việc");
        mDeactivate.setForeground(new java.awt.Color(0xEF4444));

        mEdit.addActionListener(e -> openForm(getSelected()));
        mReset.addActionListener(e -> resetPassword(getSelected()));
        mDeactivate.addActionListener(e -> deactivate(getSelected()));

        menu.add(mEdit);
        menu.add(mReset);
        menu.addSeparator();
        menu.add(mDeactivate);
        menu.show(table, x, y);
    }

    private NhanVien getSelected() {
        return model.getRow(table.getSelectedRow());
    }

    // CRUD

    public void refresh() {
        SwingWorkerHelper.run(
                employeeService::listAll,
                items -> { allItems = items; applyFilter(); },
                err -> Toast.error(this, "Lỗi: " + err.getMessage()));
    }

    private void openForm(NhanVien editing) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        EmployeeFormDialog dialog = new EmployeeFormDialog(owner, editing);
        dialog.onSave(this::saveItem);
        dialog.setVisible(true);
    }

    private void saveItem(NhanVien nv, String password) {
        boolean isAdd = nv.getId() == 0;
        SwingWorkerHelper.run(
                () -> {
                    if (isAdd) employeeService.create(nv, password);
                    else {
                        employeeService.update(nv);
                        if (password != null && !password.isEmpty()) {
                            employeeService.resetPassword(nv, password);
                        }
                    }
                    return null;
                },
                v -> {
                    Toast.success(this, isAdd ? "Đã thêm NV: " + nv.getHoTen()
                            : "Đã cập nhật: " + nv.getHoTen());
                    refresh();
                },
                err -> Toast.error(this, err.getMessage()));
    }

    private void resetPassword(NhanVien nv) {
        String newPwd = javax.swing.JOptionPane.showInputDialog(this,
                "Nhập mật khẩu mới cho " + nv.getHoTen() + ":",
                "Đặt lại mật khẩu", javax.swing.JOptionPane.QUESTION_MESSAGE);
        if (newPwd == null || newPwd.isEmpty()) return;
        if (newPwd.length() < 6) {
            Toast.error(this, "Mật khẩu tối thiểu 6 ký tự");
            return;
        }
        SwingWorkerHelper.run(
                () -> { employeeService.resetPassword(nv, newPwd); return null; },
                v -> Toast.success(this, "Đã đặt lại mật khẩu cho: " + nv.getHoTen()),
                err -> Toast.error(this, err.getMessage()));
    }

    private void deactivate(NhanVien nv) {
        boolean ok = ConfirmDialog.ask(this, "Cho nghỉ việc",
                "Đánh dấu " + nv.getHoTen() + " nghỉ việc? Có thể khôi phục bằng cách edit lại.");
        if (!ok) return;
        SwingWorkerHelper.run(
                () -> { employeeService.deactivate(nv.getId()); return null; },
                v -> { Toast.success(this, "Đã cập nhật trạng thái"); refresh(); },
                err -> Toast.error(this, err.getMessage()));
    }

    private java.util.List<NhanVien> filteredItems() {
        String query = search.getText().trim().toLowerCase();
        if (query.isEmpty()) return allItems;
        return allItems.stream()
                .filter(n -> n.getHoTen().toLowerCase().contains(query)
                          || n.getTaiKhoan().toLowerCase().contains(query))
                .toList();
    }

    private void applyFilter() {
        pagination.setTotalRows(filteredItems().size());
        renderPage();
    }

    private void renderPage() {
        var filtered = filteredItems();
        int from = (pagination.currentPage() - 1) * pagination.pageSize();
        int to = Math.min(from + pagination.pageSize(), filtered.size());
        model.setData(from < to ? filtered.subList(from, to) : java.util.List.of());
    }
}

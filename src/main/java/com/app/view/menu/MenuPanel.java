package com.app.view.menu;

import com.app.config.AppFonts;
import com.app.config.AppSpacing;
import com.app.model.DanhMuc;
import com.app.model.MonAn;
import com.app.service.MenuService;
import com.app.util.SwingWorkerHelper;
import com.app.view.common.ConfirmDialog;
import com.app.view.common.GhostButton;
import com.app.view.common.Pagination;
import com.app.view.common.PrimaryButton;
import com.app.view.common.SearchField;
import com.app.view.common.Toast;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.List;

/** Quản lý Menu — JTable + filter + dialog form. */
public class MenuPanel extends JPanel {

    private final MenuService menuService = new MenuService();
    private final MenuTableModel model = new MenuTableModel();
    private final JTable table = new JTable(model);

    private final SearchField search = new SearchField("Tìm món...");
    private final JComboBox<Object> filterCategory = new JComboBox<>();
    private final JComboBox<String> filterStatus = new JComboBox<>(
            new String[]{"Tất cả", "Đang bán", "Hết hàng"});
    private final Pagination pagination = new Pagination(15);
    private java.util.List<MonAn> allItems = new java.util.ArrayList<>();
    private java.util.List<com.app.model.DanhMuc> allCategories = new java.util.ArrayList<>();

    public MenuPanel() {
        super(new BorderLayout(0, AppSpacing.MD));
        setBorder(BorderFactory.createEmptyBorder(AppSpacing.LG, AppSpacing.LG, AppSpacing.LG, AppSpacing.LG));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(),  BorderLayout.CENTER);
        add(pagination,    BorderLayout.SOUTH);

        // Filter wiring
        search.onTextChanged(s -> applyFilter());
        filterCategory.addActionListener(e -> applyFilter());
        filterStatus.addActionListener(e -> applyFilter());
        pagination.onPageChange(p -> renderPage());
    }

    // UI

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, AppSpacing.SM));
        header.setOpaque(false);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        JLabel title = new JLabel("Quản lý Menu");
        title.setFont(AppFonts.h1 == null ? title.getFont() : AppFonts.h1);
        titleRow.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppSpacing.SM, 0));
        actions.setOpaque(false);
        PrimaryButton btnAdd = new PrimaryButton("+ Thêm món");
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
        filters.add(new JLabel("Danh mục:"));
        filters.add(filterCategory);
        filters.add(new JLabel("Tình trạng:"));
        filters.add(filterStatus);
        header.add(filters, BorderLayout.CENTER);

        return header;
    }

    private JScrollPane buildTable() {
        table.setRowHeight(36);
        // Right-click trên row → context menu (Sửa / Xóa)
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
        javax.swing.JPopupMenu menu = new javax.swing.JPopupMenu();
        javax.swing.JMenuItem mEdit = new javax.swing.JMenuItem("Sửa");
        javax.swing.JMenuItem mDelete = new javax.swing.JMenuItem("Xóa");
        mDelete.setForeground(new java.awt.Color(0xEF4444));

        mEdit.addActionListener(e -> openForm(model.getRow(table.getSelectedRow())));
        mDelete.addActionListener(e -> deleteItem(model.getRow(table.getSelectedRow())));

        menu.add(mEdit);
        menu.add(mDelete);
        menu.show(table, x, y);
    }

    // CRUD

    public void refresh() {
        SwingWorkerHelper.run(
                () -> new Object[]{ menuService.listAll(), menuService.listCategories() },
                arr -> {
                    @SuppressWarnings("unchecked")
                    List<MonAn> items = (List<MonAn>) arr[0];
                    @SuppressWarnings("unchecked")
                    List<DanhMuc> cats = (List<DanhMuc>) arr[1];
                    allItems = items;
                    allCategories = cats;

                    filterCategory.removeAllItems();
                    filterCategory.addItem("Tất cả");
                    for (DanhMuc d : cats) filterCategory.addItem(d);

                    applyFilter();
                },
                err -> Toast.error(this, "Lỗi tải danh sách: " + err.getMessage()));
    }

    private void openForm(MonAn editing) {
        SwingWorkerHelper.run(
                menuService::listCategories,
                cats -> {
                    if (cats.isEmpty()) {
                        Toast.warning(this, "Chưa có danh mục — tạo danh mục trước");
                        return;
                    }
                    Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
                    MenuFormDialog dialog = new MenuFormDialog(owner, cats, editing);
                    dialog.onSave(this::saveItem);
                    dialog.setVisible(true);
                },
                err -> Toast.error(this, err.getMessage()));
    }

    private void saveItem(MonAn item) {
        boolean isAdd = item.getId() == 0;
        SwingWorkerHelper.run(
                () -> {
                    if (isAdd) menuService.create(item);
                    else menuService.update(item);
                    return null;
                },
                v -> {
                    Toast.success(this, isAdd ? "Đã thêm: " + item.getTenMon()
                            : "Đã cập nhật: " + item.getTenMon());
                    refresh();
                },
                err -> Toast.error(this, err.getMessage()));
    }

    private void deleteItem(MonAn item) {
        boolean ok = ConfirmDialog.ask(this, "Xóa món",
                "Xóa món \"" + item.getTenMon() + "\"? Hành động này không thể hoàn tác.");
        if (!ok) return;
        SwingWorkerHelper.run(
                () -> { menuService.delete(item.getId()); return null; },
                v -> { Toast.success(this, "Đã xóa: " + item.getTenMon()); refresh(); },
                err -> Toast.error(this, err.getMessage()));
    }

    // FILTER

    private List<MonAn> filteredItems() {
        String query = search.getText().trim().toLowerCase();
        Object cat = filterCategory.getSelectedItem();
        String status = (String) filterStatus.getSelectedItem();
        return allItems.stream().filter(m -> {
            if (!query.isEmpty() && !m.getTenMon().toLowerCase().contains(query)) return false;
            if (cat instanceof DanhMuc d && m.getDanhMucId() != d.getId()) return false;
            if ("Đang bán".equals(status) && m.getConHang() != 1) return false;
            if ("Hết hàng".equals(status) && m.getConHang() != 0) return false;
            return true;
        }).toList();
    }

    private void applyFilter() {
        pagination.setTotalRows(filteredItems().size());
        renderPage();
    }

    private void renderPage() {
        List<MonAn> filtered = filteredItems();
        int from = (pagination.currentPage() - 1) * pagination.pageSize();
        int to = Math.min(from + pagination.pageSize(), filtered.size());
        List<MonAn> pageItems = from < to ? filtered.subList(from, to) : List.of();
        model.setData(pageItems, allCategories);
    }
}

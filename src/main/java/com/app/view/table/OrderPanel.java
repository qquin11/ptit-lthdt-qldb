package com.app.view.table;

import com.app.config.AppFonts;
import com.app.config.AppSpacing;
import com.app.config.Session;
import com.app.model.ChiTietHoaDon;
import com.app.model.HoaDon;
import com.app.model.MonAn;
import com.app.service.MenuService;
import com.app.service.OrderService;
import com.app.util.CurrencyFormatter;
import com.app.util.SwingWorkerHelper;
import com.app.view.common.GhostButton;
import com.app.view.common.PrimaryButton;
import com.app.view.common.SearchField;
import com.app.view.common.Toast;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * Gọi món — split 60/40 menu grid | order table với qty controls.
 * F9 chuyển sang thanh toán, Esc về sơ đồ bàn.
 */
public class OrderPanel extends JPanel {

    private final MenuService menuService = new MenuService();
    private final OrderService orderService = new OrderService();

    private final IntConsumer onProceedPayment; // pass hoaDonId
    private final Runnable onBack;

    private final JTabbedPane categoryTabs = new JTabbedPane();
    private final SearchField searchMenu = new SearchField("Tìm món...");
    private final OrderTableModel orderModel = new OrderTableModel();
    private final JTable orderTable = new JTable(orderModel);
    private final java.util.Map<Integer, java.util.List<MonAn>> menuByCategory = new java.util.LinkedHashMap<>();
    private java.util.List<MonAn> menuAll = java.util.List.of();
    private final JLabel lblHeader  = new JLabel();
    private final JLabel lblTotal   = new JLabel("0 đ");
    private final JLabel lblSubtotal = new JLabel("0 đ");
    private final JLabel lblVat     = new JLabel("0 đ");
    private final JTextField fieldDiscount = new JTextField("0", 4);

    private HoaDon currentOrder;
    private List<MonAn> currentMenu = List.of();

    public OrderPanel(IntConsumer onProceedPayment, Runnable onBack) {
        super(new BorderLayout(0, AppSpacing.MD));
        this.onProceedPayment = onProceedPayment;
        this.onBack = onBack;
        setBorder(BorderFactory.createEmptyBorder(AppSpacing.LG, AppSpacing.LG, AppSpacing.LG, AppSpacing.LG));

        add(buildHeader(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildMenuSide(), buildOrderSide());
        split.setResizeWeight(0.6);
        split.setDividerSize(6);
        add(split, BorderLayout.CENTER);

        // F9 = thanh toán
        javax.swing.InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        javax.swing.ActionMap am = getActionMap();
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F9, 0), "pay");
        am.put("pay", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { proceedPayment(); }
        });
    }

    // UI BUILDING

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        lblHeader.setFont(AppFonts.h1 == null ? lblHeader.getFont() : AppFonts.h1);
        header.add(lblHeader, BorderLayout.WEST);

        GhostButton back = new GhostButton("Quay lại sơ đồ bàn");
        back.addActionListener(e -> { if (onBack != null) onBack.run(); });
        header.add(back, BorderLayout.EAST);
        return header;
    }

    private JPanel buildMenuSide() {
        JPanel left = new JPanel(new BorderLayout(0, AppSpacing.SM));
        left.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, AppSpacing.SM));

        JPanel top = new JPanel(new BorderLayout(0, AppSpacing.XS));
        top.setOpaque(false);
        JLabel t = new JLabel("Menu món ăn");
        t.setFont(AppFonts.h2 == null ? t.getFont() : AppFonts.h2);
        top.add(t, BorderLayout.NORTH);
        top.add(searchMenu, BorderLayout.SOUTH);
        left.add(top, BorderLayout.NORTH);

        searchMenu.onTextChanged(s -> rebuildMenuTabs());
        left.add(categoryTabs, BorderLayout.CENTER);
        return left;
    }

    private JPanel buildOrderSide() {
        JPanel right = new JPanel(new BorderLayout(0, AppSpacing.SM));
        right.setBorder(BorderFactory.createEmptyBorder(0, AppSpacing.SM, 0, 0));

        JLabel t = new JLabel("Hóa đơn hiện tại");
        t.setFont(AppFonts.h2 == null ? t.getFont() : AppFonts.h2);
        right.add(t, BorderLayout.NORTH);

        orderTable.setRowHeight(36);
        orderTable.setSelectionBackground(new Color(0xD1FAE5));
        orderTable.setSelectionForeground(java.awt.Color.BLACK);
        orderTable.setRowSelectionAllowed(true);
        orderTable.setShowGrid(false);
        // SL column: inline +/- buttons cho phép click trực tiếp khỏi chọn row trước
        orderTable.getColumnModel().getColumn(1).setCellRenderer(new QtyCellRenderer());
        orderTable.getColumnModel().getColumn(1).setCellEditor(new QtyCellEditor());
        orderTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        orderTable.getColumnModel().getColumn(1).setMinWidth(110);
        // Remove column: dấu × bấm là xóa
        orderTable.getColumnModel().getColumn(4).setCellRenderer(new RemoveCellRenderer());
        orderTable.getColumnModel().getColumn(4).setCellEditor(new RemoveCellEditor());
        orderTable.getColumnModel().getColumn(4).setPreferredWidth(50);
        orderTable.getColumnModel().getColumn(4).setMaxWidth(60);
        right.add(new JScrollPane(orderTable), BorderLayout.CENTER);

        // Footer: summary + actions
        JPanel footer = new JPanel(new BorderLayout(0, AppSpacing.SM));
        footer.setOpaque(false);

        JPanel summary = new JPanel(new GridLayout(0, 2, AppSpacing.SM, 4));
        summary.setOpaque(false);
        summary.add(new JLabel("Tạm tính:"));         summary.add(rightLabel(lblSubtotal));
        summary.add(new JLabel("VAT (10%):"));        summary.add(rightLabel(lblVat));
        summary.add(new JLabel("Giảm giá (%):"));     summary.add(fieldDiscount);
        JLabel lblTotalKey = new JLabel("TỔNG:");
        lblTotalKey.setFont(AppFonts.h2 == null ? lblTotalKey.getFont() : AppFonts.h2);
        summary.add(lblTotalKey);
        lblTotal.setFont(AppFonts.h1 == null ? lblTotal.getFont() : AppFonts.h1);
        summary.add(rightLabel(lblTotal));

        footer.add(summary, BorderLayout.CENTER);

        PrimaryButton btnPay = new PrimaryButton("THANH TOÁN  (F9)");
        btnPay.addActionListener(e -> proceedPayment());
        footer.add(btnPay, BorderLayout.SOUTH);

        right.add(footer, BorderLayout.SOUTH);

        fieldDiscount.addActionListener(e -> recalc());
        return right;
    }

    private JLabel rightLabel(JLabel l) {
        l.setHorizontalAlignment(SwingConstants.RIGHT);
        return l;
    }

    // PUBLIC API

    /** Mở order cho bàn (load draft hoặc create mới). */
    public void openForTable(int banId) {
        lblHeader.setText("Bàn #" + banId + " · NV: " + Session.currentName());
        int nvId = Session.get() == null ? 1 : Session.get().getId();
        SwingWorkerHelper.run(
                () -> orderService.loadOrCreateDraft(banId, nvId),
                hd -> {
                    currentOrder = hd;
                    reloadMenu();
                    reloadItems();
                },
                err -> Toast.error(this, "Lỗi mở order: " + err.getMessage()));
    }

    // INTERNAL

    private void reloadMenu() {
        SwingWorkerHelper.run(
                () -> {
                    var cats = menuService.listCategories();
                    var all = menuService.listAvailable();
                    java.util.Map<Integer, java.util.List<MonAn>> byCat = new java.util.LinkedHashMap<>();
                    for (var c : cats) byCat.put(c.getId(), menuService.listByCategory(c.getId()));
                    return new Object[]{cats, all, byCat};
                },
                arr -> {
                    @SuppressWarnings("unchecked")
                    var cats = (java.util.List<com.app.model.DanhMuc>) arr[0];
                    @SuppressWarnings("unchecked")
                    var all = (java.util.List<MonAn>) arr[1];
                    @SuppressWarnings("unchecked")
                    var byCat = (java.util.Map<Integer, java.util.List<MonAn>>) arr[2];
                    menuAll = all;
                    menuByCategory.clear();
                    for (var c : cats) menuByCategory.put(c.getId(), byCat.getOrDefault(c.getId(), java.util.List.of()));
                    rebuildMenuTabs();
                },
                err -> {});
    }

    private void rebuildMenuTabs() {
        String q = searchMenu.getText().trim().toLowerCase();
        categoryTabs.removeAll();
        categoryTabs.addTab("Tất cả", buildMenuGrid(filterByText(menuAll, q)));
        for (var entry : menuByCategory.entrySet()) {
            int catId = entry.getKey();
            String catName = menuService.listCategories().stream()
                    .filter(c -> c.getId() == catId)
                    .findFirst().map(c -> c.getTenDanhMuc()).orElse("Khác");
            categoryTabs.addTab(catName, buildMenuGrid(filterByText(entry.getValue(), q)));
        }
    }

    private java.util.List<MonAn> filterByText(java.util.List<MonAn> items, String q) {
        if (q.isEmpty()) return items;
        return items.stream().filter(m -> m.getTenMon().toLowerCase().contains(q)).toList();
    }

    private void changeQtyRow(int row, int delta) {
        if (currentOrder == null || row < 0 || row >= orderModel.getRowCount()) return;
        var item = orderModel.getRow(row);
        int newQty = item.getSoLuong() + delta;
        SwingWorkerHelper.run(
                () -> { orderService.setQuantity(currentOrder.getId(), item.getMonAnId(), newQty); return null; },
                v -> reloadItems(),
                err -> Toast.error(this, err.getMessage()));
    }

    private JScrollPane buildMenuGrid(List<MonAn> items) {
        JPanel grid = new JPanel(new GridLayout(0, 3, AppSpacing.SM, AppSpacing.SM));
        grid.setBorder(BorderFactory.createEmptyBorder(AppSpacing.SM, AppSpacing.SM, AppSpacing.SM, AppSpacing.SM));
        for (MonAn m : items) {
            grid.add(new MenuCard(m, this::onAddItem));
        }
        return new JScrollPane(grid);
    }

    private void onAddItem(MonAn m) {
        if (currentOrder == null) return;
        SwingWorkerHelper.run(
                () -> { orderService.addItem(currentOrder.getId(), m, 1, null); return null; },
                v -> {
                    Toast.success(this, "Đã thêm: " + m.getTenMon());
                    reloadItems();
                },
                err -> Toast.error(this, "Lỗi thêm món: " + err.getMessage()));
    }

    private void reloadItems() {
        if (currentOrder == null) return;
        SwingWorkerHelper.run(
                () -> orderService.getItems(currentOrder.getId()),
                items -> { orderModel.setData(items); recalc(); },
                err -> {});
    }

    private void removeRow(int row) {
        if (currentOrder == null || row < 0 || row >= orderModel.getRowCount()) return;
        ChiTietHoaDon item = orderModel.getRow(row);
        SwingWorkerHelper.run(
                () -> { orderService.removeItem(currentOrder.getId(), item.getMonAnId()); return null; },
                v -> { Toast.info(this, "Đã xóa: " + item.getTenMon()); reloadItems(); },
                err -> Toast.error(this, err.getMessage()));
    }

    private void recalc() {
        if (currentOrder == null) return;
        double subtotal = orderModel.subtotal();
        double vat = subtotal * 0.10;
        double discountPercent = 0;
        try { discountPercent = Double.parseDouble(fieldDiscount.getText().trim()); }
        catch (NumberFormatException ignored) {}
        double discount = subtotal * (discountPercent / 100.0);
        double total = subtotal + vat - discount;

        lblSubtotal.setText(CurrencyFormatter.format(subtotal));
        lblVat.setText(CurrencyFormatter.format(vat));
        lblTotal.setText(CurrencyFormatter.format(total));
    }

    private void proceedPayment() {
        if (currentOrder == null) return;
        if (orderModel.getRowCount() == 0) {
            Toast.warning(this, "Hóa đơn trống — chưa có món");
            return;
        }
        if (onProceedPayment != null) onProceedPayment.accept(currentOrder.getId());
    }

    // TABLE MODEL

    private static class OrderTableModel extends AbstractTableModel {
        private final String[] cols = {"Món", "Số lượng", "Đơn giá", "Thành tiền", ""};
        private List<ChiTietHoaDon> data = new ArrayList<>();

        public void setData(List<ChiTietHoaDon> d) {
            this.data = d == null ? new ArrayList<>() : d;
            fireTableDataChanged();
        }

        public ChiTietHoaDon getRow(int i) { return data.get(i); }

        public double subtotal() {
            return data.stream().mapToDouble(ChiTietHoaDon::getThanhTien).sum();
        }

        @Override public int getRowCount()    { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        // Cell 1 (qty) và cell 4 (remove) phải editable thì cell editor mới nhận click
        @Override public boolean isCellEditable(int r, int c) { return c == 1 || c == 4; }

        @Override
        public Object getValueAt(int r, int c) {
            ChiTietHoaDon item = data.get(r);
            return switch (c) {
                case 0 -> item.getTenMon();
                case 1 -> item.getSoLuong();
                case 2 -> CurrencyFormatter.format(item.getGiaLucMua());
                case 3 -> CurrencyFormatter.format(item.getThanhTien());
                default -> "";
            };
        }
    }

    // Renderer cho cột Số lượng: hiển thị "−  N  +" inline
    private class QtyCellRenderer extends JPanel implements TableCellRenderer {
        private final JLabel lblQty = new JLabel("0", SwingConstants.CENTER);
        private final JLabel lblMinus = new JLabel("−", SwingConstants.CENTER);
        private final JLabel lblPlus  = new JLabel("+", SwingConstants.CENTER);
        QtyCellRenderer() {
            super(new GridLayout(1, 3, 4, 0));
            setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            for (var l : new JLabel[]{lblMinus, lblPlus}) {
                l.setOpaque(true);
                l.setBackground(new Color(0xE5E7EB));
                l.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
            }
            add(lblMinus); add(lblQty); add(lblPlus);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            lblQty.setText(value == null ? "0" : value.toString());
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return this;
        }
    }

    // Editor cho cột Số lượng: clickable −/+ trực tiếp
    private class QtyCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new GridLayout(1, 3, 4, 0));
        private final JLabel lblQty = new JLabel("0", SwingConstants.CENTER);
        private final JButton btnMinus = new JButton("−");
        private final JButton btnPlus  = new JButton("+");
        private int editingRow = -1;
        QtyCellEditor() {
            panel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            for (JButton b : new JButton[]{btnMinus, btnPlus}) {
                b.setFocusPainted(false);
                b.setMargin(new java.awt.Insets(0, 4, 0, 4));
                b.putClientProperty("JButton.buttonType", "borderless");
            }
            btnMinus.addActionListener(e -> { changeQtyRow(editingRow, -1); fireEditingStopped(); });
            btnPlus.addActionListener(e ->  { changeQtyRow(editingRow, +1); fireEditingStopped(); });
            panel.add(btnMinus); panel.add(lblQty); panel.add(btnPlus);
        }
        @Override public Object getCellEditorValue() { return lblQty.getText(); }
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            editingRow = row;
            lblQty.setText(value == null ? "0" : value.toString());
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }
    }

    // Renderer cho cột xóa: dấu ×
    private static class RemoveCellRenderer extends JLabel implements TableCellRenderer {
        RemoveCellRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setText("×");
            setForeground(new Color(0xEF4444));
            setFont(getFont().deriveFont(java.awt.Font.BOLD, 18f));
            setOpaque(true);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return this;
        }
    }

    // Editor cho cột xóa: click là xóa luôn
    private class RemoveCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton btn = new JButton("×");
        private int editingRow = -1;
        RemoveCellEditor() {
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setContentAreaFilled(false);
            btn.setForeground(new Color(0xEF4444));
            btn.setFont(btn.getFont().deriveFont(java.awt.Font.BOLD, 18f));
            btn.addActionListener(e -> { removeRow(editingRow); fireEditingStopped(); });
        }
        @Override public Object getCellEditorValue() { return null; }
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            editingRow = row;
            return btn;
        }
    }
}

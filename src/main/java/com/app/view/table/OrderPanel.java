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
import com.app.view.common.SecondaryButton;
import com.app.view.common.Toast;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * Gọi món ⭐ — split 60/40 menu grid | order table với qty controls.
 * F9 chuyển sang thanh toán, Esc về sơ đồ bàn.
 */
public class OrderPanel extends JPanel {

    private final MenuService menuService = new MenuService();
    private final OrderService orderService = new OrderService();

    private final IntConsumer onProceedPayment; // pass hoaDonId
    private final Runnable onBack;

    private final JTabbedPane categoryTabs = new JTabbedPane();
    private final OrderTableModel orderModel = new OrderTableModel();
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
    }

    // ===================== UI BUILDING =====================

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        lblHeader.setFont(AppFonts.h1 == null ? lblHeader.getFont() : AppFonts.h1);
        header.add(lblHeader, BorderLayout.WEST);

        GhostButton back = new GhostButton("← Quay lại sơ đồ bàn");
        back.addActionListener(e -> { if (onBack != null) onBack.run(); });
        header.add(back, BorderLayout.EAST);
        return header;
    }

    private JPanel buildMenuSide() {
        JPanel left = new JPanel(new BorderLayout(0, AppSpacing.SM));
        left.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, AppSpacing.SM));
        JLabel t = new JLabel("Menu món ăn");
        t.setFont(AppFonts.h2 == null ? t.getFont() : AppFonts.h2);
        left.add(t, BorderLayout.NORTH);
        left.add(categoryTabs, BorderLayout.CENTER);
        return left;
    }

    private JPanel buildOrderSide() {
        JPanel right = new JPanel(new BorderLayout(0, AppSpacing.SM));
        right.setBorder(BorderFactory.createEmptyBorder(0, AppSpacing.SM, 0, 0));

        JLabel t = new JLabel("Hóa đơn hiện tại");
        t.setFont(AppFonts.h2 == null ? t.getFont() : AppFonts.h2);
        right.add(t, BorderLayout.NORTH);

        JTable table = new JTable(orderModel);
        table.setRowHeight(32);
        right.add(new JScrollPane(table), BorderLayout.CENTER);

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

        JPanel buttons = new JPanel(new GridLayout(1, 3, AppSpacing.SM, 0));
        buttons.setOpaque(false);
        SecondaryButton btnRefresh = new SecondaryButton("↻ Tính lại");
        btnRefresh.addActionListener(e -> recalc());
        SecondaryButton btnRemove  = new SecondaryButton("🗑 Xóa món chọn");
        btnRemove.addActionListener(e -> removeSelected(table.getSelectedRow()));
        PrimaryButton btnPay = new PrimaryButton("💳 THANH TOÁN (F9)");
        btnPay.addActionListener(e -> proceedPayment());

        buttons.add(btnRefresh);
        buttons.add(btnRemove);
        buttons.add(btnPay);
        footer.add(buttons, BorderLayout.SOUTH);

        right.add(footer, BorderLayout.SOUTH);

        fieldDiscount.addActionListener(e -> recalc());
        return right;
    }

    private JLabel rightLabel(JLabel l) {
        l.setHorizontalAlignment(SwingConstants.RIGHT);
        return l;
    }

    // ===================== PUBLIC API =====================

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

    // ===================== INTERNAL =====================

    private void reloadMenu() {
        SwingWorkerHelper.run(
                menuService::listCategories,
                cats -> {
                    categoryTabs.removeAll();
                    // Tab "Tất cả"
                    categoryTabs.addTab("Tất cả", buildMenuGrid(menuService.listAvailable()));
                    for (var c : cats) {
                        categoryTabs.addTab(c.getTenDanhMuc(),
                                buildMenuGrid(menuService.listByCategory(c.getId())));
                    }
                },
                err -> {});
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

    private void removeSelected(int row) {
        if (currentOrder == null || row < 0) return;
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

    // ===================== TABLE MODEL =====================

    private static class OrderTableModel extends AbstractTableModel {
        private final String[] cols = {"Món", "SL", "Đơn giá", "Thành tiền"};
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
}

package com.app.view.payment;

import com.app.config.AppFonts;
import com.app.config.AppSpacing;
import com.app.dao.HoaDonDAO;
import com.app.model.ChiTietHoaDon;
import com.app.model.HoaDon;
import com.app.service.OrderService;
import com.app.service.PaymentService;
import com.app.util.CurrencyFormatter;
import com.app.util.SwingWorkerHelper;
import com.app.view.common.ConfirmDialog;
import com.app.view.common.GhostButton;
import com.app.view.common.PrimaryButton;
import com.app.view.common.SecondaryButton;
import com.app.view.common.Toast;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Thanh toán: split chi tiết|tính tiền. Quick amount + change realtime.
 */
public class PaymentPanel extends JPanel {

    private final PaymentService paymentService = new PaymentService();
    private final OrderService orderService = new OrderService();
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();

    private final Runnable onAfterPay;

    private final DetailModel detailModel = new DetailModel();
    private final JLabel lblSubtotal = new JLabel("0 đ");
    private final JLabel lblVat      = new JLabel("0 đ");
    private final JLabel lblTotal    = new JLabel("0 đ");
    private final JLabel lblChange   = new JLabel("0 đ");
    private final JTextField fieldMoney    = new JTextField(12);
    private final JTextField fieldDiscount = new JTextField("0", 4);
    private final ButtonGroup methodGroup  = new ButtonGroup();
    private String currentMethod = HoaDon.PT_TIEN_MAT;

    private HoaDon currentOrder;

    public PaymentPanel(Runnable onAfterPay) {
        super(new BorderLayout(0, AppSpacing.MD));
        this.onAfterPay = onAfterPay;
        setBorder(BorderFactory.createEmptyBorder(AppSpacing.LG, AppSpacing.LG, AppSpacing.LG, AppSpacing.LG));

        JLabel title = new JLabel("Thanh toán");
        title.setFont(AppFonts.h1 == null ? title.getFont() : AppFonts.h1);
        add(title, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildDetail(), buildCalculator());
        split.setResizeWeight(0.6);
        split.setDividerSize(6);
        add(split, BorderLayout.CENTER);

        // Realtime recalc khi gõ tiền khách / discount
        DocumentListener recalcOnChange = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { recalc(); }
            @Override public void removeUpdate(DocumentEvent e) { recalc(); }
            @Override public void changedUpdate(DocumentEvent e) { recalc(); }
        };
        fieldMoney.getDocument().addDocumentListener(recalcOnChange);
        fieldDiscount.getDocument().addDocumentListener(recalcOnChange);
    }

    private JPanel buildDetail() {
        JPanel p = new JPanel(new BorderLayout(0, AppSpacing.SM));
        p.setBorder(BorderFactory.createEmptyBorder(AppSpacing.SM, 0, 0, AppSpacing.SM));
        JLabel t = new JLabel("Chi tiết hóa đơn");
        t.setFont(AppFonts.h2 == null ? t.getFont() : AppFonts.h2);
        p.add(t, BorderLayout.NORTH);
        JTable table = new JTable(detailModel);
        table.setRowHeight(30);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildCalculator() {
        JPanel p = new JPanel(new BorderLayout(0, AppSpacing.MD));
        p.setBorder(BorderFactory.createEmptyBorder(AppSpacing.SM, AppSpacing.SM, 0, 0));
        JLabel t = new JLabel("Tính tiền");
        t.setFont(AppFonts.h2 == null ? t.getFont() : AppFonts.h2);
        p.add(t, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(0, 2, AppSpacing.SM, AppSpacing.SM));
        form.setOpaque(false);
        form.add(new JLabel("Tạm tính:"));        form.add(rightLabel(lblSubtotal));
        form.add(new JLabel("VAT (10%):"));       form.add(rightLabel(lblVat));
        form.add(new JLabel("Giảm giá (%):"));    form.add(fieldDiscount);
        JLabel keyTotal = new JLabel("TỔNG:");
        keyTotal.setFont(AppFonts.h2 == null ? keyTotal.getFont() : AppFonts.h2);
        form.add(keyTotal);
        lblTotal.setFont(AppFonts.h1 == null ? lblTotal.getFont() : AppFonts.h1);
        form.add(rightLabel(lblTotal));
        form.add(new JLabel("Tiền khách đưa:"));  form.add(fieldMoney);
        form.add(new JLabel("Tiền thừa:"));       form.add(rightLabel(lblChange));

        // Quick amount
        JPanel quick = new JPanel(new FlowLayout(FlowLayout.LEFT, AppSpacing.XS, 0));
        quick.setOpaque(false);
        for (int amt : new int[]{50_000, 100_000, 200_000, 500_000, 1_000_000}) {
            GhostButton b = new GhostButton(CurrencyFormatter.formatPlain(amt));
            b.addActionListener(e -> fieldMoney.setText(String.valueOf(amt)));
            quick.add(b);
        }

        // Payment method
        JPanel methods = new JPanel(new FlowLayout(FlowLayout.LEFT, AppSpacing.SM, 0));
        methods.setOpaque(false);
        addMethod(methods, "Tiền mặt",      HoaDon.PT_TIEN_MAT, true);
        addMethod(methods, "Chuyển khoản",  HoaDon.PT_CHUYEN_KHOAN, false);
        addMethod(methods, "Thẻ",           HoaDon.PT_THE, false);

        // Buttons
        JPanel buttons = new JPanel(new GridLayout(1, 2, AppSpacing.SM, 0));
        buttons.setOpaque(false);
        SecondaryButton btnPreview = new SecondaryButton("Xem hóa đơn");
        btnPreview.addActionListener(e -> showInvoicePreview());
        PrimaryButton btnConfirm   = new PrimaryButton("XÁC NHẬN THANH TOÁN");
        btnConfirm.addActionListener(e -> confirmPayment());
        buttons.add(btnPreview);
        buttons.add(btnConfirm);

        JPanel center = new JPanel(new BorderLayout(0, AppSpacing.SM));
        center.setOpaque(false);
        center.add(form, BorderLayout.NORTH);
        JPanel mid = new JPanel(new BorderLayout(0, AppSpacing.SM));
        mid.setOpaque(false);
        mid.add(quick, BorderLayout.NORTH);
        mid.add(methods, BorderLayout.SOUTH);
        center.add(mid, BorderLayout.CENTER);
        center.add(buttons, BorderLayout.SOUTH);

        p.add(center, BorderLayout.CENTER);
        return p;
    }

    private void addMethod(JPanel parent, String label, String value, boolean selected) {
        JRadioButton rb = new JRadioButton(label, selected);
        rb.addActionListener(e -> currentMethod = value);
        methodGroup.add(rb);
        parent.add(rb);
    }

    private JLabel rightLabel(JLabel l) {
        l.setHorizontalAlignment(SwingConstants.RIGHT);
        return l;
    }

    // PUBLIC API

    public void openForOrder(int hoaDonId) {
        SwingWorkerHelper.run(
                () -> {
                    HoaDon hd = hoaDonDAO.findById(hoaDonId);
                    List<ChiTietHoaDon> items = orderService.getItems(hoaDonId);
                    return new Object[]{hd, items};
                },
                arr -> {
                    currentOrder = (HoaDon) arr[0];
                    @SuppressWarnings("unchecked")
                    var items = (List<ChiTietHoaDon>) arr[1];
                    detailModel.setData(items);
                    fieldMoney.setText("");
                    fieldDiscount.setText("0");
                    recalc();
                },
                err -> Toast.error(this, "Lỗi mở thanh toán: " + err.getMessage()));
    }

    // INTERNAL

    private void recalc() {
        double subtotal = detailModel.subtotal();
        double vat = subtotal * 0.10;
        double discountPercent = parseSafe(fieldDiscount.getText(), 0);
        double discount = subtotal * (discountPercent / 100.0);
        double total = subtotal + vat - discount;
        double money = parseSafe(fieldMoney.getText(), 0);
        double change = money - total;

        lblSubtotal.setText(CurrencyFormatter.format(subtotal));
        lblVat.setText(CurrencyFormatter.format(vat));
        lblTotal.setText(CurrencyFormatter.format(total));
        if (change < 0) {
            lblChange.setText("Thiếu " + CurrencyFormatter.format(-change));
            lblChange.setForeground(Color.RED);
        } else {
            lblChange.setText(CurrencyFormatter.format(change));
            lblChange.setForeground(null);
        }
    }

    private double parseSafe(String s, double dflt) {
        try { return Double.parseDouble(s.replace(".", "").replace(",", "").trim()); }
        catch (Exception e) { return dflt; }
    }

    private void confirmPayment() {
        if (currentOrder == null) {
            Toast.warning(this, "Chưa có hóa đơn để thanh toán");
            return;
        }
        double subtotal = detailModel.subtotal();
        if (subtotal <= 0) {
            Toast.warning(this, "Hóa đơn trống");
            return;
        }
        double discountPercent = parseSafe(fieldDiscount.getText(), 0);
        double discount = subtotal * (discountPercent / 100.0);
        double money = parseSafe(fieldMoney.getText(), 0);

        currentOrder.setTongTien(subtotal);
        currentOrder.setVat(0.10);
        currentOrder.setGiamGia(discount);

        if (money < currentOrder.getThanhToanCuoi()) {
            Toast.error(this, "Tiền khách đưa không đủ");
            return;
        }
        if (!ConfirmDialog.ask(this, "Xác nhận thanh toán",
                "Xác nhận thanh toán HĐ #" + currentOrder.getId() + "?",
                "Thanh toán", "Hủy", false)) return;

        SwingWorkerHelper.run(
                () -> paymentService.finalize(currentOrder, money, discount, currentMethod),
                hd -> {
                    Toast.success(this, "Thanh toán thành công · HĐ #" + hd.getId());
                    showInvoicePreview();
                    if (onAfterPay != null) onAfterPay.run();
                },
                err -> Toast.error(this, err.getMessage()));
    }

    private void showInvoicePreview() {
        if (currentOrder == null) return;
        new InvoicePreviewDialog(currentOrder, detailModel.snapshot()).setVisible(true);
    }

    // TABLE MODEL

    private static class DetailModel extends AbstractTableModel {
        private final String[] cols = {"Tên món", "SL", "Đơn giá", "Thành tiền"};
        private List<ChiTietHoaDon> data = new ArrayList<>();

        public void setData(List<ChiTietHoaDon> d) {
            this.data = d == null ? new ArrayList<>() : d;
            fireTableDataChanged();
        }

        public List<ChiTietHoaDon> snapshot() { return new ArrayList<>(data); }

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

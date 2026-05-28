package com.app.view.payment;

import com.app.config.AppConfig;
import com.app.config.AppFonts;
import com.app.config.AppSpacing;
import com.app.config.Session;
import com.app.model.ChiTietHoaDon;
import com.app.model.HoaDon;
import com.app.util.CurrencyFormatter;
import com.app.view.common.GhostButton;
import com.app.view.common.PrimaryButton;
import com.app.view.common.Toast;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.print.PrinterException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Preview hóa đơn format 80mm (32 chars wide monospace) + nút In. */
public class InvoicePreviewDialog extends JDialog {

    private static final int LINE_WIDTH = 36;

    public InvoicePreviewDialog(HoaDon hd, List<ChiTietHoaDon> items) {
        super((java.awt.Frame) null, "Xem trước hóa đơn #" + hd.getId(), true);
        setSize(AppConfig.INVOICE_WIDTH, AppConfig.INVOICE_HEIGHT);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTextArea ta = new JTextArea(buildReceipt(hd, items));
        ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        ta.setEditable(false);
        ta.setBorder(BorderFactory.createEmptyBorder(
                AppSpacing.MD, AppSpacing.MD, AppSpacing.MD, AppSpacing.MD));
        add(new JScrollPane(ta), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppSpacing.SM, AppSpacing.SM));
        GhostButton btnClose = new GhostButton("Đóng");
        btnClose.addActionListener(e -> dispose());
        PrimaryButton btnPrint = new PrimaryButton("In hóa đơn");
        btnPrint.addActionListener(e -> {
            try {
                ta.print();
            } catch (PrinterException ex) {
                Toast.error(this, "In lỗi: " + ex.getMessage());
            }
        });
        buttons.add(btnClose);
        buttons.add(btnPrint);
        add(buttons, BorderLayout.SOUTH);
    }

    private String buildReceipt(HoaDon hd, List<ChiTietHoaDon> items) {
        StringBuilder sb = new StringBuilder();
        sb.append(center(com.app.config.AppSettings.restaurantName())).append('\n');
        sb.append(center(com.app.config.AppSettings.restaurantAddress())).append('\n');
        sb.append(center("ĐT: " + com.app.config.AppSettings.restaurantPhone())).append('\n');
        sb.append(line('=')).append('\n');
        sb.append("HĐ: #").append(hd.getId()).append('\n');
        sb.append("Ngày: ").append(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append('\n');
        sb.append("NV: ").append(Session.currentName())
          .append(" · Bàn ").append(hd.getBanAnId()).append('\n');
        sb.append(line('-')).append('\n');

        for (ChiTietHoaDon c : items) {
            String name = c.getTenMon();
            if (name == null) name = "Món #" + c.getMonAnId();
            sb.append(padRight(name, 20))
              .append(" x").append(c.getSoLuong()).append(' ')
              .append(padLeft(CurrencyFormatter.formatPlain(c.getThanhTien()), LINE_WIDTH - 24)).append('\n');
        }

        sb.append(line('-')).append('\n');
        sb.append(kv("Tạm tính",     CurrencyFormatter.formatPlain(hd.getTongTien()))).append('\n');
        sb.append(kv("VAT " + (int)(hd.getVat()*100) + "%",
                CurrencyFormatter.formatPlain(hd.getTongTien() * hd.getVat()))).append('\n');
        if (hd.getGiamGia() > 0) {
            sb.append(kv("Giảm giá", "-" + CurrencyFormatter.formatPlain(hd.getGiamGia()))).append('\n');
        }
        sb.append(kv("TỔNG", CurrencyFormatter.formatPlain(hd.getThanhToanCuoi()))).append('\n');

        if (hd.getTienKhachDua() > 0) {
            sb.append(kv("Tiền khách", CurrencyFormatter.formatPlain(hd.getTienKhachDua()))).append('\n');
            sb.append(kv("Tiền thừa",  CurrencyFormatter.formatPlain(hd.getTienThua()))).append('\n');
        }
        sb.append(kv("Phương thức", labelPt(hd.getPhuongThucTt()))).append('\n');
        sb.append(line('=')).append('\n');
        sb.append(center("CẢM ƠN QUÝ KHÁCH")).append('\n');
        sb.append(center("Hẹn gặp lại!")).append('\n');
        return sb.toString();
    }

    private static String line(char c) {
        return String.valueOf(c).repeat(LINE_WIDTH);
    }

    private static String center(String s) {
        if (s.length() >= LINE_WIDTH) return s;
        int pad = (LINE_WIDTH - s.length()) / 2;
        return " ".repeat(pad) + s;
    }

    private static String padLeft(String s, int width) {
        return s.length() >= width ? s : " ".repeat(width - s.length()) + s;
    }

    private static String padRight(String s, int width) {
        if (s.length() >= width) return s.substring(0, width);
        return s + " ".repeat(width - s.length());
    }

    private static String kv(String key, String value) {
        int valWidth = LINE_WIDTH - key.length() - 1;
        return key + " " + padLeft(value, valWidth);
    }

    private static String labelPt(String pt) {
        if (pt == null) return "Tiền mặt";
        return switch (pt) {
            case HoaDon.PT_CHUYEN_KHOAN -> "Chuyển khoản";
            case HoaDon.PT_THE -> "Thẻ";
            default -> "Tiền mặt";
        };
    }
}

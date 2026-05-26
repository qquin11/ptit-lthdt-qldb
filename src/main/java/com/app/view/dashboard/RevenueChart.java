package com.app.view.dashboard;

import com.app.config.AppColors;
import com.app.config.AppFonts;
import com.app.model.DatabaseHelper;
import com.app.util.CurrencyFormatter;
import com.app.util.SwingWorkerHelper;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Bar chart doanh thu 7 ngày qua — vẽ Graphics2D, không cần lib. */
public class RevenueChart extends JPanel {

    private double[] values = new double[7];
    private String[] labels = new String[7];
    private double maxValue = 1;

    public RevenueChart() {
        setPreferredSize(new Dimension(0, 220));
        setBorder(BorderFactory.createTitledBorder("Doanh thu 7 ngày qua"));
        for (int i = 0; i < 7; i++) labels[i] = "";
    }

    public void refresh() {
        SwingWorkerHelper.run(
                this::loadLast7Days,
                arr -> {
                    values = arr;
                    maxValue = 1;
                    for (double v : values) if (v > maxValue) maxValue = v;
                    repaint();
                },
                err -> {});
    }

    private double[] loadLast7Days() {
        double[] vals = new double[7];
        LocalDate today = LocalDate.now();
        var fmt = DateTimeFormatter.ofPattern("dd/MM");
        for (int i = 0; i < 7; i++) {
            LocalDate day = today.minusDays(6 - i);
            labels[i] = day.format(fmt);
            try (PreparedStatement ps = DatabaseHelper.getConnection().prepareStatement(
                    "SELECT COALESCE(SUM(tong_tien * (1 + vat) - giam_gia), 0) " +
                    "FROM hoa_don WHERE trang_thai = 1 AND DATE(ngay_tao) = ?")) {
                ps.setString(1, day.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) vals[i] = rs.getDouble(1);
                }
            } catch (Exception ignored) {}
        }
        return vals;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int padTop = 30, padBot = 40, padLeft = 60, padRight = 20;
        int w = getWidth(), h = getHeight();
        int chartW = w - padLeft - padRight;
        int chartH = h - padTop - padBot;
        int barCount = values.length;
        int barW = Math.max(10, (chartW - (barCount + 1) * 10) / barCount);

        // Trục
        g2.setColor(new Color(0, 0, 0, 40));
        g2.drawLine(padLeft, h - padBot, w - padRight, h - padBot);

        // Vẽ bars
        Font small = AppFonts.small == null ? g2.getFont() : AppFonts.small;
        g2.setFont(small);
        for (int i = 0; i < barCount; i++) {
            double frac = values[i] / maxValue;
            int barH = (int) (chartH * frac);
            int x = padLeft + 10 + i * (barW + 10);
            int y = h - padBot - barH;

            g2.setColor(AppColors.LIGHT_ACCENT);
            g2.fillRoundRect(x, y, barW, barH, 6, 6);

            // Label dưới
            g2.setColor(new Color(0, 0, 0, 140));
            String lab = labels[i];
            int lx = x + (barW - g2.getFontMetrics().stringWidth(lab)) / 2;
            g2.drawString(lab, lx, h - padBot + 16);

            // Giá trị trên bar nếu > 0
            if (values[i] > 0) {
                String v = CurrencyFormatter.formatPlain(values[i] / 1000) + "k";
                int vx = x + (barW - g2.getFontMetrics().stringWidth(v)) / 2;
                g2.drawString(v, vx, y - 4);
            }
        }
        g2.dispose();
    }
}

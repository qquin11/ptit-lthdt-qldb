package com.app.view.table;

import com.app.config.AppColors;
import com.app.config.AppFonts;
import com.app.config.AppSpacing;
import com.app.model.BanAn;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/** Card hiển thị 1 bàn — số bàn + status dot + label + click handler. */
public class TableCard extends JPanel {

    private final BanAn ban;
    private final JLabel lblName;
    private final JLabel lblStatus;
    private final JLabel lblDot;
    private final Color statusColor;
    private boolean hover = false;

    public TableCard(BanAn ban, Consumer<BanAn> onClick) {
        super(new BorderLayout(0, AppSpacing.XS));
        this.ban = ban;
        this.statusColor = colorOf(ban.getTrangThai());

        setPreferredSize(new Dimension(160, 130));
        setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(statusColor, 2, true),
                BorderFactory.createEmptyBorder(AppSpacing.MD, AppSpacing.MD, AppSpacing.MD, AppSpacing.MD)));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        lblName = new JLabel(ban.getTenBan(), SwingConstants.LEFT);
        lblName.setFont(AppFonts.h2 == null ? lblName.getFont() : AppFonts.h2);

        lblDot = new JLabel("●");
        lblDot.setForeground(statusColor);
        lblDot.setFont(AppFonts.h2 == null ? lblDot.getFont() : AppFonts.h2);

        lblStatus = new JLabel(labelOf(ban.getTrangThai()));
        lblStatus.setFont(AppFonts.small == null ? lblStatus.getFont() : AppFonts.small);

        // Layout: [name top] [dot + status center]
        JPanel center = new JPanel(new GridLayout(2, 1, 0, AppSpacing.XS));
        center.setOpaque(false);
        center.add(lblDot);
        center.add(lblStatus);

        add(lblName, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onClick.accept(ban); }
            @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
            @Override public void mouseExited (MouseEvent e) { hover = false; repaint(); }
        });
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        Color bg = hover ? new Color(statusColor.getRed(), statusColor.getGreen(), statusColor.getBlue(), 30)
                         : new Color(statusColor.getRed(), statusColor.getGreen(), statusColor.getBlue(), 12);
        g.setColor(bg);
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

    public BanAn getBanAn() { return ban; }

    private static Color colorOf(String trangThai) {
        return switch (trangThai == null ? "" : trangThai) {
            case "DANG_DUNG" -> AppColors.TABLE_OCCUPIED;
            case "DA_DAT"    -> AppColors.TABLE_RESERVED;
            default          -> AppColors.TABLE_EMPTY;
        };
    }

    private static String labelOf(String trangThai) {
        return switch (trangThai == null ? "" : trangThai) {
            case "DANG_DUNG" -> "Đang dùng";
            case "DA_DAT"    -> "Đã đặt";
            default          -> "Trống";
        };
    }
}

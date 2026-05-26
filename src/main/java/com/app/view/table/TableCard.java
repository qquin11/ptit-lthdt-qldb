package com.app.view.table;

import com.app.config.AppColors;
import com.app.config.AppFonts;
import com.app.config.AppSpacing;
import com.app.model.BanAn;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/** Card hiển thị 1 bàn — chuột trái mở Order, chuột phải mở menu (Đặt/Thanh toán). */
public class TableCard extends JPanel {

    private final BanAn ban;
    private final Color statusColor;
    private final Consumer<BanAn> onClickHandler;
    private final Consumer<BanAn> onReserveHandler;
    private final Consumer<BanAn> onPayHandler;
    private boolean hover = false;

    public TableCard(BanAn ban, Consumer<BanAn> onClick) {
        this(ban, onClick, null, null);
    }

    public TableCard(BanAn ban, Consumer<BanAn> onClick,
                     Consumer<BanAn> onReserve, Consumer<BanAn> onPay) {
        super(new BorderLayout(0, AppSpacing.XS));
        this.ban = ban;
        this.statusColor = colorOf(ban.getTrangThai());
        this.onClickHandler = onClick;
        this.onReserveHandler = onReserve;
        this.onPayHandler = onPay;

        setPreferredSize(new Dimension(160, 130));
        setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(statusColor, 2, true),
                BorderFactory.createEmptyBorder(AppSpacing.MD, AppSpacing.MD, AppSpacing.MD, AppSpacing.MD)));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lblName = new JLabel(ban.getTenBan(), SwingConstants.LEFT);
        lblName.setFont(AppFonts.h2 == null ? lblName.getFont() : AppFonts.h2);

        JLabel lblDot = new JLabel("●");
        lblDot.setForeground(statusColor);
        lblDot.setFont(AppFonts.h2 == null ? lblDot.getFont() : AppFonts.h2);

        JLabel lblStatus = new JLabel(labelOf(ban.getTrangThai()));
        lblStatus.setFont(AppFonts.small == null ? lblStatus.getFont() : AppFonts.small);

        JPanel center = new JPanel(new GridLayout(2, 1, 0, AppSpacing.XS));
        center.setOpaque(false);
        center.add(lblDot);
        center.add(lblStatus);

        add(lblName, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    showContextMenu(e);
                } else if (onClickHandler != null) {
                    onClickHandler.accept(ban);
                }
            }
            @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
            @Override public void mouseExited (MouseEvent e) { hover = false; repaint(); }
        });
    }

    private void showContextMenu(MouseEvent e) {
        JPopupMenu menu = new JPopupMenu();
        String trangThai = ban.getTrangThai();
        boolean trong = "TRONG".equals(trangThai);
        boolean dang = "DANG_DUNG".equals(trangThai);

        JMenuItem mOpen = new JMenuItem(dang ? "Tiếp tục order" : "Gọi món");
        mOpen.addActionListener(ev -> { if (onClickHandler != null) onClickHandler.accept(ban); });
        menu.add(mOpen);

        if (trong && onReserveHandler != null) {
            JMenuItem mReserve = new JMenuItem("Đặt trước bàn này");
            mReserve.addActionListener(ev -> onReserveHandler.accept(ban));
            menu.add(mReserve);
        }
        if (dang && onPayHandler != null) {
            JMenuItem mPay = new JMenuItem("Thanh toán bàn");
            mPay.addActionListener(ev -> onPayHandler.accept(ban));
            menu.add(mPay);
        }
        menu.show(this, e.getX(), e.getY());
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

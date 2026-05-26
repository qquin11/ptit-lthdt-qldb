package com.app.view.dashboard;

import com.app.config.AppFonts;
import com.app.config.AppSpacing;
import com.app.dao.HoaDonDAO;
import com.app.model.HoaDon;
import com.app.util.CurrencyFormatter;
import com.app.util.DateFormatter;
import com.app.util.SwingWorkerHelper;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import java.awt.BorderLayout;
import java.awt.Component;

/** Feed các hóa đơn gần nhất. */
public class ActivityFeed extends JPanel {

    private final DefaultListModel<HoaDon> model = new DefaultListModel<>();
    private final JList<HoaDon> list = new JList<>(model);
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();

    public ActivityFeed() {
        super(new BorderLayout());
        putClientProperty("FlatLaf.style", "arc: 12");
        setBorder(BorderFactory.createTitledBorder("Hoạt động gần đây"));

        list.setCellRenderer(new Renderer());
        list.setFixedCellHeight(36);
        add(new JScrollPane(list), BorderLayout.CENTER);
    }

    public void refresh() {
        SwingWorkerHelper.run(
                () -> hoaDonDAO.findRecent(10),
                items -> {
                    model.clear();
                    for (HoaDon h : items) model.addElement(h);
                },
                err -> model.clear());
    }

    private static class Renderer extends JLabel implements ListCellRenderer<HoaDon> {
        Renderer() {
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(
                    AppSpacing.SM, AppSpacing.MD, AppSpacing.SM, AppSpacing.MD));
        }
        @Override
        public Component getListCellRendererComponent(JList<? extends HoaDon> jList, HoaDon h,
                                                       int index, boolean selected, boolean focused) {
            String status = h.getTrangThai() == 1 ? "✓" : "⏳";
            String time = DateFormatter.fromSql(h.getNgayTao()) != null
                    ? DateFormatter.TIME.format(DateFormatter.fromSql(h.getNgayTao()))
                    : "--:--";
            setText(String.format("  %s  %s  · HĐ #%d · Bàn %d · %s",
                    status, time, h.getId(), h.getBanAnId(),
                    CurrencyFormatter.format(h.getThanhToanCuoi())));
            setFont(AppFonts.body == null ? getFont() : AppFonts.body);
            setBackground(selected ? jList.getSelectionBackground() : jList.getBackground());
            setForeground(selected ? jList.getSelectionForeground() : jList.getForeground());
            return this;
        }
    }
}

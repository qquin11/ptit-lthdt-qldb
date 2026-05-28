package com.app.view.table;

import com.app.config.AppFonts;
import com.app.config.AppSpacing;
import com.app.dao.HoaDonDAO;
import com.app.model.BanAn;
import com.app.model.HoaDon;
import com.app.service.TableService;
import com.app.util.SwingWorkerHelper;
import com.app.view.common.SearchField;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

/** Sơ đồ bàn — grid + filter + live timer 60s update tổng tiền/thời gian cho bàn đang dùng. */
public class TableMapPanel extends JPanel {

    private final TableService tableService = new TableService();
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final IntConsumer onTableClicked;
    private IntConsumer onReserveRequested;
    private IntConsumer onPayRequested;

    private final JComboBox<String> filterStatus = new JComboBox<>(new String[]{
            "Tất cả", "Trống", "Đang dùng", "Đã đặt"});
    private final SearchField search = new SearchField("Tìm bàn (vd: Bàn 5)...");
    // FlowLayout tự xuống dòng khi window narrow → cards giữ preferred size khỏi bị bóp
    private final JPanel grid = new JPanel(new FlowLayout(FlowLayout.LEFT, AppSpacing.MD, AppSpacing.MD));
    private final JLabel legend = new JLabel(" ");

    private List<BanAn> allTables = List.of();
    private Map<Integer, HoaDon> openOrdersByBan = new HashMap<>();
    private final Timer tickTimer;

    public TableMapPanel(IntConsumer onTableClicked) {
        super(new BorderLayout(0, AppSpacing.MD));
        this.onTableClicked = onTableClicked;
        setBorder(BorderFactory.createEmptyBorder(AppSpacing.LG, AppSpacing.LG, AppSpacing.LG, AppSpacing.LG));

        JPanel header = new JPanel(new BorderLayout(0, AppSpacing.SM));
        header.setOpaque(false);
        JLabel title = new JLabel("Sơ đồ bàn");
        title.setFont(AppFonts.h1 == null ? title.getFont() : AppFonts.h1);
        header.add(title, BorderLayout.NORTH);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, AppSpacing.SM, 0));
        filterBar.setOpaque(false);
        filterBar.add(new JLabel("Trạng thái:"));
        filterBar.add(filterStatus);
        filterBar.add(search);
        header.add(filterBar, BorderLayout.CENTER);

        legend.setFont(AppFonts.small == null ? legend.getFont() : AppFonts.small);
        header.add(legend, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);

        grid.setOpaque(false);
        add(new JScrollPane(grid), BorderLayout.CENTER);

        filterStatus.addActionListener(e -> render());
        search.onTextChanged(s -> render());

        // Tick mỗi 60s update timer/tổng tiền cho bàn đang dùng
        tickTimer = new Timer(60_000, e -> tickCards());
        tickTimer.setRepeats(true);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        tickTimer.start();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        tickTimer.stop();
    }

    public void refresh() {
        SwingWorkerHelper.run(
                () -> {
                    List<BanAn> tables = tableService.listAll();
                    List<HoaDon> openOrders = hoaDonDAO.findAllOpen();
                    Map<Integer, HoaDon> map = new HashMap<>();
                    for (HoaDon h : openOrders) map.put(h.getBanAnId(), h);
                    return new Object[]{tables, map};
                },
                arr -> {
                    @SuppressWarnings("unchecked")
                    List<BanAn> tables = (List<BanAn>) arr[0];
                    @SuppressWarnings("unchecked")
                    Map<Integer, HoaDon> map = (Map<Integer, HoaDon>) arr[1];
                    allTables = tables;
                    openOrdersByBan = map;
                    render();
                },
                err -> { allTables = List.of(); openOrdersByBan = new HashMap<>(); render(); });
    }

    private void tickCards() {
        for (var comp : grid.getComponents()) {
            if (comp instanceof TableCard tc) tc.updateOrderInfo();
        }
    }

    private void render() {
        grid.removeAll();
        String filter = (String) filterStatus.getSelectedItem();
        String query = search.getText().trim().toLowerCase();

        int countEmpty = 0, countOcc = 0, countRes = 0;
        for (BanAn b : allTables) {
            if ("TRONG".equals(b.getTrangThai())) countEmpty++;
            else if ("DANG_DUNG".equals(b.getTrangThai())) countOcc++;
            else if ("DA_DAT".equals(b.getTrangThai())) countRes++;
            if (!matches(b, filter, query)) continue;
            HoaDon order = openOrdersByBan.get(b.getId());
            grid.add(new TableCard(b, order, this::handleClick,
                    onReserveRequested == null ? null : ban -> onReserveRequested.accept(ban.getId()),
                    onPayRequested == null ? null : ban -> onPayRequested.accept(ban.getId())));
        }

        legend.setText(String.format(
                "<html><font color='#10B981'>●</font> Trống (%d) &nbsp; " +
                "<font color='#F59E0B'>●</font> Đang dùng (%d) &nbsp; " +
                "<font color='#6366F1'>●</font> Đã đặt (%d)</html>",
                countEmpty, countOcc, countRes));

        grid.revalidate();
        grid.repaint();
    }

    private boolean matches(BanAn b, String filter, String query) {
        boolean statusOk = switch (filter == null ? "Tất cả" : filter) {
            case "Trống"     -> "TRONG".equals(b.getTrangThai());
            case "Đang dùng" -> "DANG_DUNG".equals(b.getTrangThai());
            case "Đã đặt"    -> "DA_DAT".equals(b.getTrangThai());
            default          -> true;
        };
        boolean nameOk = query.isEmpty() || b.getTenBan().toLowerCase().contains(query);
        return statusOk && nameOk;
    }

    private void handleClick(BanAn b) {
        if (onTableClicked != null) onTableClicked.accept(b.getId());
    }

    public void setOnReserveRequested(IntConsumer h) { this.onReserveRequested = h; }
    public void setOnPayRequested(IntConsumer h) { this.onPayRequested = h; }
}

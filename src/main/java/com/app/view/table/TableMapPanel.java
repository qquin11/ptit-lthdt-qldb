package com.app.view.table;

import com.app.config.AppColors;
import com.app.config.AppFonts;
import com.app.config.AppSpacing;
import com.app.model.BanAn;
import com.app.service.TableService;
import com.app.util.SwingWorkerHelper;
import com.app.view.common.SearchField;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * Sơ đồ bàn ⭐. Filter status + search + grid cards. Click bàn → mở Order.
 */
public class TableMapPanel extends JPanel {

    private final TableService tableService = new TableService();
    private final IntConsumer onTableClicked;
    private IntConsumer onReserveRequested;
    private IntConsumer onPayRequested;

    private final JComboBox<String> filterStatus = new JComboBox<>(new String[]{
            "Tất cả", "Trống", "Đang dùng", "Đã đặt"});
    private final SearchField search = new SearchField("Tìm bàn (vd: Bàn 5)...");
    private final JPanel grid = new JPanel(new GridLayout(0, 5, AppSpacing.MD, AppSpacing.MD));
    private final JLabel legend = new JLabel(" ");

    private List<BanAn> allTables = List.of();

    public TableMapPanel(IntConsumer onTableClicked) {
        super(new BorderLayout(0, AppSpacing.MD));
        this.onTableClicked = onTableClicked;
        setBorder(BorderFactory.createEmptyBorder(AppSpacing.LG, AppSpacing.LG, AppSpacing.LG, AppSpacing.LG));

        // Header
        JPanel header = new JPanel(new BorderLayout(0, AppSpacing.SM));
        header.setOpaque(false);
        JLabel title = new JLabel("Sơ đồ bàn");
        title.setFont(AppFonts.h1 == null ? title.getFont() : AppFonts.h1);
        header.add(title, BorderLayout.NORTH);

        // Filter bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, AppSpacing.SM, 0));
        filterBar.setOpaque(false);
        filterBar.add(new JLabel("Trạng thái:"));
        filterBar.add(filterStatus);
        filterBar.add(search);
        header.add(filterBar, BorderLayout.CENTER);

        legend.setFont(AppFonts.small == null ? legend.getFont() : AppFonts.small);
        header.add(legend, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);

        // Grid
        grid.setOpaque(false);
        add(new JScrollPane(grid), BorderLayout.CENTER);

        // Wire filters
        filterStatus.addActionListener(e -> render());
        search.onTextChanged(s -> render());
    }

    public void refresh() {
        SwingWorkerHelper.run(
                tableService::listAll,
                tables -> { allTables = tables; render(); },
                err -> { allTables = List.of(); render(); });
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
            grid.add(new TableCard(b, this::handleClick,
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
        // dummy to silence unused field warning
        Object _c = AppColors.TABLE_EMPTY;
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

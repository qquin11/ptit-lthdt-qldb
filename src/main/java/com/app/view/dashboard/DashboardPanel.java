package com.app.view.dashboard;

import com.app.config.AppColors;
import com.app.config.AppFonts;
import com.app.config.AppSpacing;
import com.app.service.PaymentService;
import com.app.service.TableService;
import com.app.util.CurrencyFormatter;
import com.app.util.SwingWorkerHelper;
import com.app.view.common.StatCard;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Dashboard với 3 KPI cards + activity feed (Phase 07). */
public class DashboardPanel extends JPanel {

    private final StatCard cardRevenue = new StatCard("💰", "Doanh thu hôm nay");
    private final StatCard cardTables  = new StatCard("🪑", "Bàn đang dùng");
    private final StatCard cardOrders  = new StatCard("📋", "Hóa đơn hôm nay");
    private final RevenueChart chart = new RevenueChart();
    private final ActivityFeed feed = new ActivityFeed();
    private final JLabel lblDateTime;

    private final PaymentService paymentService = new PaymentService();
    private final TableService tableService = new TableService();

    public DashboardPanel() {
        super(new BorderLayout(0, AppSpacing.LG));
        setBorder(BorderFactory.createEmptyBorder(
                AppSpacing.LG, AppSpacing.LG, AppSpacing.LG, AppSpacing.LG));

        // Header: title + datetime
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Trang chủ");
        title.setFont(AppFonts.h1 == null ? title.getFont() : AppFonts.h1);
        lblDateTime = new JLabel("", SwingConstants.RIGHT);
        lblDateTime.setFont(AppFonts.body == null ? lblDateTime.getFont() : AppFonts.body);
        header.add(title, BorderLayout.WEST);
        header.add(lblDateTime, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // 3 KPI cards row
        JPanel cards = new JPanel(new GridLayout(1, 3, AppSpacing.LG, 0));
        cards.setOpaque(false);
        cardRevenue.setAccent(AppColors.SUCCESS);
        cardTables.setAccent(AppColors.WARNING);
        cardOrders.setAccent(AppColors.INFO);
        cards.add(cardRevenue);
        cards.add(cardTables);
        cards.add(cardOrders);

        JPanel body = new JPanel(new BorderLayout(0, AppSpacing.LG));
        body.setOpaque(false);
        body.add(cards, BorderLayout.NORTH);
        body.add(chart, BorderLayout.CENTER);
        body.add(feed,  BorderLayout.SOUTH);
        add(body, BorderLayout.CENTER);

        Timer clock = new Timer(1000, e ->
                lblDateTime.setText("🕐 " + LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("HH:mm:ss · dd/MM/yyyy"))));
        clock.start();
    }

    /** Reload data — gọi mỗi lần hiển thị panel. */
    public void refresh() {
        SwingWorkerHelper.run(
                () -> new Object[] {
                        paymentService.sumTodayRevenue(),
                        tableService.listAll(),
                        paymentService.countToday()
                },
                arr -> {
                    cardRevenue.setValue(CurrencyFormatter.format((double) arr[0]));
                    @SuppressWarnings("unchecked")
                    var tables = (java.util.List<com.app.model.BanAn>) arr[1];
                    long occ = tables.stream().filter(t -> "DANG_DUNG".equals(t.getTrangThai())).count();
                    cardTables.setValue(occ + "/" + tables.size());
                    cardOrders.setValue(String.valueOf((int) arr[2]));
                    feed.refresh();
                    chart.refresh();
                },
                err -> {
                    cardRevenue.setValue("--");
                    cardTables.setValue("--");
                    cardOrders.setValue("--");
                });
    }
}

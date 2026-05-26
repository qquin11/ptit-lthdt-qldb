package com.app.view.main;

import com.app.config.AppConfig;
import com.app.view.dashboard.DashboardPanel;
import com.app.view.employee.EmployeePanel;
import com.app.view.menu.MenuPanel;
import com.app.view.payment.PaymentPanel;
import com.app.view.reservation.ReservationPanel;
import com.app.view.table.OrderPanel;
import com.app.view.table.TableMapPanel;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Main app shell sau khi đăng nhập. BorderLayout: TopBar(N) + Sidebar(W) +
 * Content CardLayout(C) + StatusBar(S).
 */
public class MainFrame extends JFrame {

    private final Sidebar   sidebar;
    private final TopBar    topBar;
    private final StatusBar statusBar;
    private final JPanel    content;
    private final CardLayout cards;

    // Panels (lazy init nếu nặng — Phase 1 init eagerly cho đơn giản)
    private final DashboardPanel   dashboardPanel;
    private final TableMapPanel    tableMapPanel;
    private final OrderPanel       orderPanel;
    private final MenuPanel        menuPanel;
    private final EmployeePanel    employeePanel;
    private final ReservationPanel reservationPanel;
    private final PaymentPanel     paymentPanel;

    public MainFrame() {
        setTitle(AppConfig.APP_NAME + " — " + AppConfig.APP_VERSION);
        setSize(AppConfig.MAIN_WIDTH, AppConfig.MAIN_HEIGHT);
        setMinimumSize(new Dimension(AppConfig.MAIN_MIN_WIDTH, AppConfig.MAIN_MIN_HEIGHT));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        sidebar   = new Sidebar();
        topBar    = new TopBar();
        statusBar = new StatusBar();

        // Content cards
        cards   = new CardLayout();
        content = new JPanel(cards);

        dashboardPanel   = new DashboardPanel();
        tableMapPanel    = new TableMapPanel(this::openOrderForTable);
        orderPanel       = new OrderPanel(this::openPaymentForOrder, this::backToTables);
        menuPanel        = new MenuPanel();
        employeePanel    = new EmployeePanel();
        reservationPanel = new ReservationPanel();
        paymentPanel     = new PaymentPanel(this::backToTables);

        content.add(dashboardPanel,   Sidebar.ID_DASHBOARD);
        content.add(tableMapPanel,    Sidebar.ID_TABLES);
        content.add(orderPanel,       "order");
        content.add(menuPanel,        Sidebar.ID_MENU);
        content.add(employeePanel,    Sidebar.ID_EMPLOYEES);
        content.add(reservationPanel, Sidebar.ID_RESERVATION);
        content.add(paymentPanel,     Sidebar.ID_PAYMENT);

        add(topBar,    BorderLayout.NORTH);
        add(sidebar,   BorderLayout.WEST);
        add(content,   BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        sidebar.setActive(Sidebar.ID_DASHBOARD);
        registerGlobalShortcuts();
    }

    private void registerGlobalShortcuts() {
        JComponent root = (JComponent) getContentPane();
        // F1 = help
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "help");
        root.getActionMap().put("help", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new HelpDialog(MainFrame.this).setVisible(true);
            }
        });
        // F5 = refresh current
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "refresh");
        root.getActionMap().put("refresh", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshCurrent();
            }
        });
    }

    private String currentCard = Sidebar.ID_DASHBOARD;

    private void refreshCurrent() {
        showPanel(currentCard);
    }

    // ===================== Card navigation API =====================

    public void showPanel(String id) {
        cards.show(content, id);
        topBar.setBreadcrumb(breadcrumbFor(id));
        currentCard = id;
        if (Sidebar.ID_DASHBOARD.equals(id))  dashboardPanel.refresh();
        if (Sidebar.ID_TABLES.equals(id))     tableMapPanel.refresh();
        if (Sidebar.ID_MENU.equals(id))       menuPanel.refresh();
        if (Sidebar.ID_EMPLOYEES.equals(id))  employeePanel.refresh();
        if (Sidebar.ID_RESERVATION.equals(id)) reservationPanel.refresh();
    }

    private void openOrderForTable(int banId) {
        orderPanel.openForTable(banId);
        cards.show(content, "order");
        topBar.setBreadcrumb("Sơ đồ bàn / Gọi món");
        sidebar.setActive(Sidebar.ID_TABLES);
    }

    private void openPaymentForOrder(int hoaDonId) {
        paymentPanel.openForOrder(hoaDonId);
        cards.show(content, Sidebar.ID_PAYMENT);
        topBar.setBreadcrumb("Sơ đồ bàn / Thanh toán");
        sidebar.setActive(Sidebar.ID_PAYMENT);
    }

    private void backToTables() {
        showPanel(Sidebar.ID_TABLES);
        sidebar.setActive(Sidebar.ID_TABLES);
    }

    private String breadcrumbFor(String id) {
        return switch (id) {
            case Sidebar.ID_DASHBOARD   -> "Trang chủ";
            case Sidebar.ID_TABLES      -> "Sơ đồ bàn";
            case Sidebar.ID_RESERVATION -> "Đặt bàn trước";
            case Sidebar.ID_MENU        -> "Quản lý Menu";
            case Sidebar.ID_EMPLOYEES   -> "Quản lý Nhân viên";
            case Sidebar.ID_PAYMENT     -> "Thanh toán";
            case Sidebar.ID_SETTINGS    -> "Cài đặt";
            default -> "Trang chủ";
        };
    }

    // ===================== Getters cho Controller =====================

    public Sidebar   getSidebarPanel() { return sidebar; }
    public TopBar    getTopBarPanel()  { return topBar; }
    public StatusBar getStatusBar()    { return statusBar; }

    @Override
    public void dispose() {
        statusBar.stop();
        super.dispose();
    }
}

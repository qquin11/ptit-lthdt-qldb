package com.app.view.reservation;

import com.app.config.AppFonts;
import com.app.config.AppSpacing;
import com.app.dao.KhachHangDAO;
import com.app.model.DatBan;
import com.app.model.KhachHang;
import com.app.service.ReservationService;
import com.app.util.DateFormatter;
import com.app.util.SwingWorkerHelper;
import com.app.view.common.ConfirmDialog;
import com.app.view.common.GhostButton;
import com.app.view.common.SecondaryButton;
import com.app.view.common.Toast;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Danh sách booking — xem, hủy, check-in. */
public class BookingListDialog extends JDialog {

    private final ReservationService reservationService = new ReservationService();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final BookingTableModel model = new BookingTableModel();
    private final JTable table = new JTable(model);

    public BookingListDialog(Frame owner) {
        super(owner, "Danh sách đặt bàn", true);
        setSize(720, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(0, AppSpacing.SM));

        JLabel title = new JLabel("  Danh sách đặt bàn");
        title.setFont(AppFonts.h1 == null ? title.getFont() : AppFonts.h1);
        title.setBorder(BorderFactory.createEmptyBorder(AppSpacing.MD, AppSpacing.MD, 0, 0));
        add(title, BorderLayout.NORTH);

        table.setRowHeight(32);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppSpacing.SM, AppSpacing.SM));
        GhostButton btnRefresh = new GhostButton("Tải lại");
        btnRefresh.addActionListener(e -> refresh());
        SecondaryButton btnCheckin = new SecondaryButton("Đã đến");
        btnCheckin.addActionListener(e -> checkin());
        SecondaryButton btnCancel = new SecondaryButton("Hủy đặt");
        btnCancel.addActionListener(e -> cancel());
        SecondaryButton btnClose = new SecondaryButton("Đóng");
        btnClose.addActionListener(e -> dispose());

        buttons.add(btnRefresh);
        buttons.add(btnCheckin);
        buttons.add(btnCancel);
        buttons.add(btnClose);
        add(buttons, BorderLayout.SOUTH);

        refresh();
    }

    private void refresh() {
        SwingWorkerHelper.run(
                () -> {
                    List<DatBan> list = reservationService.listAll();
                    Map<Integer, String> customerNames = new HashMap<>();
                    for (KhachHang k : khachHangDAO.findAll()) {
                        customerNames.put(k.getId(), k.getHoTen() + " · " + k.getSoDienThoai());
                    }
                    return new Object[]{list, customerNames};
                },
                arr -> {
                    @SuppressWarnings("unchecked")
                    List<DatBan> list = (List<DatBan>) arr[0];
                    @SuppressWarnings("unchecked")
                    Map<Integer, String> names = (Map<Integer, String>) arr[1];
                    model.setData(list, names);
                },
                err -> Toast.error(this, "Lỗi tải danh sách: " + err.getMessage()));
    }

    private DatBan selected() {
        int row = table.getSelectedRow();
        return row < 0 ? null : model.getRow(row);
    }

    private void cancel() {
        DatBan d = selected();
        if (d == null) { Toast.warning(this, "Chọn booking trước"); return; }
        if ("HUY".equals(d.getTrangThai())) { Toast.info(this, "Booking đã hủy"); return; }
        if (!ConfirmDialog.ask(this, "Hủy booking", "Hủy booking #" + d.getId() + "?")) return;
        SwingWorkerHelper.run(
                () -> reservationService.cancel(d.getId()),
                ok -> { Toast.success(this, "Đã hủy"); refresh(); },
                err -> Toast.error(this, err.getMessage()));
    }

    private void checkin() {
        DatBan d = selected();
        if (d == null) { Toast.warning(this, "Chọn booking trước"); return; }
        if (!"CHO_DEN".equals(d.getTrangThai())) {
            Toast.info(this, "Chỉ booking đang chờ mới check-in được"); return;
        }
        SwingWorkerHelper.run(
                () -> reservationService.checkin(d.getId()),
                ok -> { Toast.success(this, "Đã đánh dấu khách đến"); refresh(); },
                err -> Toast.error(this, err.getMessage()));
    }

    private static class BookingTableModel extends AbstractTableModel {
        private final String[] cols = {"Mã", "Khách hàng", "Bàn", "Thời gian", "Ghi chú", "Trạng thái"};
        private List<DatBan> data = new ArrayList<>();
        private Map<Integer, String> customerNames = new HashMap<>();

        void setData(List<DatBan> d, Map<Integer, String> names) {
            this.data = d == null ? new ArrayList<>() : d;
            this.customerNames = names == null ? new HashMap<>() : names;
            fireTableDataChanged();
        }

        DatBan getRow(int i) { return data.get(i); }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            DatBan d = data.get(r);
            return switch (c) {
                case 0 -> d.getId();
                case 1 -> customerNames.getOrDefault(d.getKhachHangId(), "#" + d.getKhachHangId());
                case 2 -> "Bàn " + d.getBanAnId();
                case 3 -> formatTime(d.getThoiGianDat());
                case 4 -> d.getGhiChu() == null ? "" : d.getGhiChu();
                case 5 -> labelStatus(d.getTrangThai());
                default -> "";
            };
        }

        private String formatTime(String sql) {
            var dt = DateFormatter.fromSql(sql);
            return dt == null ? sql : DateFormatter.formatDateTime(dt);
        }

        private String labelStatus(String s) {
            return switch (s == null ? "" : s) {
                case "DA_NHAN" -> "Đã đến";
                case "HUY" -> "Đã hủy";
                default -> "Chờ đến";
            };
        }
    }
}

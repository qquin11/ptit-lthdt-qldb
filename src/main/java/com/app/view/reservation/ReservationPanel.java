package com.app.view.reservation;

import com.app.config.AppColors;
import com.app.config.AppFonts;
import com.app.config.AppSpacing;
import com.app.dao.BanAnDAO;
import com.app.model.BanAn;
import com.app.service.ReservationService;
import com.app.util.SwingWorkerHelper;
import com.app.view.common.PrimaryButton;
import com.app.view.common.SecondaryButton;
import com.app.view.common.Toast;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Đặt bàn trước — split form (40%) + bàn trống grid (60%).
 * Khi đổi ngày/giờ → refresh grid bàn trống. Click bàn trống → fill field.
 */
public class ReservationPanel extends JPanel {

    private final ReservationService reservationService = new ReservationService();
    private final BanAnDAO banAnDAO = new BanAnDAO();

    private final JTextField fieldName  = new JTextField();
    private final JTextField fieldPhone = new JTextField();
    private final JSpinner   spinnerDateTime = createDateTimeSpinner();
    private final JLabel     lblSelectedTable = new JLabel("(chưa chọn)");
    private final JTextArea  fieldNote = new JTextArea(3, 20);
    private final JPanel     tableGrid = new JPanel(new GridLayout(0, 5, AppSpacing.SM, AppSpacing.SM));

    private Integer selectedBanId = null;

    public ReservationPanel() {
        super(new BorderLayout(0, AppSpacing.MD));
        setBorder(BorderFactory.createEmptyBorder(AppSpacing.LG, AppSpacing.LG, AppSpacing.LG, AppSpacing.LG));

        JLabel title = new JLabel("Đặt bàn trước");
        title.setFont(AppFonts.h1 == null ? title.getFont() : AppFonts.h1);
        add(title, BorderLayout.NORTH);

        javax.swing.JSplitPane split = new javax.swing.JSplitPane(
                javax.swing.JSplitPane.HORIZONTAL_SPLIT, buildForm(), buildAvailability());
        split.setResizeWeight(0.4);
        split.setDividerSize(6);
        add(split, BorderLayout.CENTER);

        spinnerDateTime.addChangeListener(e -> refreshAvailability());
    }

    public void refresh() {
        refreshAvailability();
    }

    // ===================== UI =====================

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setLayout(new GridLayout(0, 1, 0, AppSpacing.XS));
        form.setBorder(BorderFactory.createEmptyBorder(AppSpacing.MD, 0, 0, AppSpacing.LG));

        form.add(label("Tên khách *"));
        form.add(fieldName);

        form.add(label("Số điện thoại *"));
        form.add(fieldPhone);

        form.add(label("Ngày & giờ đặt *"));
        form.add(spinnerDateTime);

        form.add(label("Bàn được chọn"));
        lblSelectedTable.setForeground(AppColors.LIGHT_ACCENT);
        form.add(lblSelectedTable);

        form.add(label("Ghi chú"));
        fieldNote.setLineWrap(true);
        fieldNote.setWrapStyleWord(true);
        form.add(new JScrollPane(fieldNote));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppSpacing.SM, AppSpacing.MD));
        buttons.setOpaque(false);
        SecondaryButton btnClear = new SecondaryButton("Xóa form");
        btnClear.addActionListener(e -> clearForm());
        PrimaryButton btnSubmit = new PrimaryButton("✓ Đặt bàn");
        btnSubmit.addActionListener(e -> submit());
        buttons.add(btnClear);
        buttons.add(btnSubmit);
        form.add(buttons);

        return form;
    }

    private JPanel buildAvailability() {
        JPanel right = new JPanel(new BorderLayout(0, AppSpacing.SM));
        right.setBorder(BorderFactory.createEmptyBorder(AppSpacing.MD, AppSpacing.LG, 0, 0));

        JLabel t = new JLabel("Bàn trống tại thời điểm này");
        t.setFont(AppFonts.h2 == null ? t.getFont() : AppFonts.h2);
        right.add(t, BorderLayout.NORTH);

        tableGrid.setOpaque(false);
        right.add(new JScrollPane(tableGrid), BorderLayout.CENTER);
        return right;
    }

    private JSpinner createDateTimeSpinner() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR_OF_DAY, 1); // mặc định = giờ tới
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        JSpinner s = new JSpinner(new SpinnerDateModel(c.getTime(), null, null, Calendar.HOUR));
        s.setEditor(new JSpinner.DateEditor(s, "dd/MM/yyyy HH:mm"));
        return s;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppFonts.small == null ? l.getFont() : AppFonts.small);
        return l;
    }

    // ===================== LOGIC =====================

    private void refreshAvailability() {
        LocalDateTime when = getSelectedDateTime();
        if (when == null) return;
        SwingWorkerHelper.run(
                () -> new Object[]{ banAnDAO.findAll(), reservationService.getBusyTableIds(when) },
                arr -> {
                    @SuppressWarnings("unchecked")
                    List<BanAn> tables = (List<BanAn>) arr[0];
                    @SuppressWarnings("unchecked")
                    Set<Integer> busy = Set.copyOf((List<Integer>) arr[1]);
                    renderGrid(tables, busy);
                },
                err -> Toast.error(this, "Lỗi tải bàn: " + err.getMessage()));
    }

    private void renderGrid(List<BanAn> tables, Set<Integer> busyIds) {
        tableGrid.removeAll();
        for (BanAn b : tables) {
            boolean free = !busyIds.contains(b.getId()) && "TRONG".equals(b.getTrangThai());
            tableGrid.add(createCard(b, free));
        }
        tableGrid.revalidate();
        tableGrid.repaint();
    }

    private JPanel createCard(BanAn b, boolean free) {
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(100, 70));
        java.awt.Color color = free ? AppColors.SUCCESS : AppColors.NEUTRAL;
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(color, 2, true),
                BorderFactory.createEmptyBorder(AppSpacing.SM, AppSpacing.SM, AppSpacing.SM, AppSpacing.SM)));

        JLabel name = new JLabel(b.getTenBan(), javax.swing.SwingConstants.CENTER);
        name.setFont(AppFonts.body == null ? name.getFont() : AppFonts.body);
        JLabel status = new JLabel(free ? "Trống" : "Bận", javax.swing.SwingConstants.CENTER);
        status.setFont(AppFonts.small == null ? status.getFont() : AppFonts.small);
        status.setForeground(color);
        card.add(name, BorderLayout.CENTER);
        card.add(status, BorderLayout.SOUTH);

        if (free) {
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedBanId = b.getId();
                    lblSelectedTable.setText(b.getTenBan());
                }
            });
        }
        return card;
    }

    private LocalDateTime getSelectedDateTime() {
        Object val = spinnerDateTime.getValue();
        if (!(val instanceof Date d)) return null;
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private void submit() {
        if (selectedBanId == null) {
            Toast.warning(this, "Vui lòng chọn bàn trống");
            return;
        }
        String name = fieldName.getText().trim();
        String phone = fieldPhone.getText().trim();
        LocalDateTime when = getSelectedDateTime();
        String note = fieldNote.getText().trim();

        SwingWorkerHelper.run(
                () -> reservationService.create(name, phone, selectedBanId, when, note),
                booking -> {
                    Toast.success(this, "Đã đặt bàn #" + booking.getId());
                    clearForm();
                    refreshAvailability();
                },
                err -> Toast.error(this, err.getMessage()));
    }

    private void clearForm() {
        fieldName.setText("");
        fieldPhone.setText("");
        fieldNote.setText("");
        selectedBanId = null;
        lblSelectedTable.setText("(chưa chọn)");
    }
}

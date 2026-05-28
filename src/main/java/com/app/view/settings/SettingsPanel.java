package com.app.view.settings;

import com.app.config.AppFonts;
import com.app.config.AppSettings;
import com.app.config.AppSpacing;
import com.app.config.ThemeManager;
import com.app.view.common.PrimaryButton;
import com.app.view.common.Toast;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

/**
 * Màn Cài đặt — 3 nhóm: thông tin nhà hàng (in trên hóa đơn), giao diện (theme),
 * tham số hóa đơn (VAT). Lưu qua {@link AppSettings}.
 */
public class SettingsPanel extends JPanel {

    private final JTextField fieldName  = new JTextField();
    private final JTextField fieldAddr  = new JTextField();
    private final JTextField fieldPhone = new JTextField();
    private final JRadioButton rbLight  = new JRadioButton("Sáng (Light)");
    private final JRadioButton rbDark   = new JRadioButton("Tối (Dark)");
    private final JSpinner spinnerVat    = new JSpinner(new SpinnerNumberModel(10, 0, 100, 1));

    public SettingsPanel() {
        super(new BorderLayout(0, AppSpacing.MD));
        setBorder(BorderFactory.createEmptyBorder(AppSpacing.LG, AppSpacing.LG, AppSpacing.LG, AppSpacing.LG));

        JLabel title = new JLabel("Cài đặt");
        title.setFont(AppFonts.h1 == null ? title.getFont() : AppFonts.h1);
        add(title, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new javax.swing.BoxLayout(body, javax.swing.BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.add(buildRestaurantSection());
        body.add(javax.swing.Box.createVerticalStrut(AppSpacing.LG));
        body.add(buildAppearanceSection());
        body.add(javax.swing.Box.createVerticalStrut(AppSpacing.LG));
        body.add(buildInvoiceSection());
        body.add(javax.swing.Box.createVerticalStrut(AppSpacing.LG));

        PrimaryButton btnSave = new PrimaryButton("Lưu cài đặt");
        btnSave.addActionListener(e -> save());
        JPanel saveRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        saveRow.setOpaque(false);
        saveRow.setAlignmentX(LEFT_ALIGNMENT);
        saveRow.add(btnSave);
        body.add(saveRow);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        reloadFields();
    }

    /** Nạp lại giá trị hiện tại — gọi mỗi lần mở panel. */
    public void reloadFields() {
        fieldName.setText(AppSettings.restaurantName());
        fieldAddr.setText(AppSettings.restaurantAddress());
        fieldPhone.setText(AppSettings.restaurantPhone());
        if (ThemeManager.current() == ThemeManager.Theme.DARK) rbDark.setSelected(true);
        else rbLight.setSelected(true);
        spinnerVat.setValue((int) Math.round(AppSettings.vatRate() * 100));
    }

    // SECTIONS

    private JPanel buildRestaurantSection() {
        JPanel form = section("Thông tin nhà hàng (in trên hóa đơn)");
        form.add(label("Tên nhà hàng"));
        form.add(fieldName);
        form.add(label("Địa chỉ"));
        form.add(fieldAddr);
        form.add(label("Số điện thoại"));
        form.add(fieldPhone);
        return form;
    }

    private JPanel buildAppearanceSection() {
        JPanel form = section("Giao diện");
        ButtonGroup group = new ButtonGroup();
        group.add(rbLight);
        group.add(rbDark);
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, AppSpacing.LG, 0));
        row.setOpaque(false);
        row.add(rbLight);
        row.add(rbDark);
        form.add(row);
        return form;
    }

    private JPanel buildInvoiceSection() {
        JPanel form = section("Tham số hóa đơn");
        form.add(label("Thuế VAT (%)"));
        spinnerVat.setPreferredSize(new Dimension(80, AppSpacing.H_INPUT));
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.add(spinnerVat);
        form.add(row);
        return form;
    }

    // HELPERS

    private JPanel section(String heading) {
        JPanel p = new JPanel(new GridLayout(0, 1, 0, AppSpacing.XS));
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(heading),
                BorderFactory.createEmptyBorder(AppSpacing.SM, AppSpacing.MD, AppSpacing.MD, AppSpacing.MD)));
        p.setMaximumSize(new Dimension(560, Integer.MAX_VALUE));
        return p;
    }

    private JComponent label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppFonts.small == null ? l.getFont() : AppFonts.small);
        return l;
    }

    private void save() {
        AppSettings.setRestaurantInfo(fieldName.getText(), fieldAddr.getText(), fieldPhone.getText());
        AppSettings.setVatPercent(((Number) spinnerVat.getValue()).doubleValue());
        ThemeManager.apply(rbDark.isSelected() ? ThemeManager.Theme.DARK : ThemeManager.Theme.LIGHT);
        Toast.success(this, "Đã lưu cài đặt");
    }
}

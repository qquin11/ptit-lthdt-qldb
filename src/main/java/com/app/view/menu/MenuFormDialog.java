package com.app.view.menu;

import com.app.config.AppFonts;
import com.app.config.AppSpacing;
import com.app.model.DanhMuc;
import com.app.model.MonAn;
import com.app.util.CurrencyFormatter;
import com.app.util.ValidationUtils;
import com.app.view.common.PrimaryButton;
import com.app.view.common.SecondaryButton;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.Consumer;

/**
 * Modal Add/Edit món ăn. Validation inline (border đỏ + error label dưới field).
 * Schema: ten_mon, gia_tien, danh_muc_id, con_hang.
 */
public class MenuFormDialog extends JDialog {

    private final JTextField fieldName = new JTextField();
    private final JTextField fieldPrice = new JTextField();
    private final JComboBox<DanhMuc> comboCategory = new JComboBox<>();
    private final JCheckBox cbAvailable = new JCheckBox("Đang bán", true);
    private final JLabel errName  = newError();
    private final JLabel errPrice = newError();

    private final MonAn editing; // null = add mode
    private Consumer<MonAn> onSave = m -> {};

    public MenuFormDialog(Frame owner, List<DanhMuc> categories, MonAn editing) {
        super(owner, editing == null ? "Thêm món mới" : "Sửa món #" + editing.getId(), true);
        this.editing = editing;
        setSize(420, 380);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        for (DanhMuc c : categories) comboCategory.addItem(c);
        // Renderer: hiển thị tên
        comboCategory.setRenderer((list, value, index, selected, focused) -> {
            JLabel l = new JLabel(value == null ? "" : value.getTenDanhMuc());
            l.setOpaque(true);
            if (selected) {
                l.setBackground(list.getSelectionBackground());
                l.setForeground(list.getSelectionForeground());
            }
            l.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            return l;
        });

        add(buildForm(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        if (editing != null) prefill(editing);
        SwingUtilities.invokeLater(fieldName::requestFocusInWindow);
    }

    public void onSave(Consumer<MonAn> handler) { this.onSave = handler; }

    // UI

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setLayout(new GridLayout(0, 1, 0, AppSpacing.XS));
        form.setBorder(BorderFactory.createEmptyBorder(
                AppSpacing.LG, AppSpacing.LG, AppSpacing.MD, AppSpacing.LG));

        form.add(label("Tên món *"));
        form.add(fieldName);
        form.add(errName);

        form.add(label("Danh mục *"));
        form.add(comboCategory);
        // (Không error label cho combo — luôn có lựa chọn nếu list non-empty)

        form.add(label("Giá (VND) *"));
        form.add(fieldPrice);
        form.add(errPrice);

        form.add(cbAvailable);
        return form;
    }

    private JPanel buildButtons() {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppSpacing.SM, AppSpacing.SM));
        buttons.setBorder(BorderFactory.createEmptyBorder(0, AppSpacing.LG, AppSpacing.LG, AppSpacing.LG));

        SecondaryButton btnCancel = new SecondaryButton("Hủy");
        btnCancel.addActionListener(e -> dispose());
        PrimaryButton btnSave = new PrimaryButton("Lưu");
        btnSave.addActionListener(e -> trySave());

        buttons.add(btnCancel);
        buttons.add(btnSave);
        return buttons;
    }

    private void prefill(MonAn m) {
        fieldName.setText(m.getTenMon());
        fieldPrice.setText(CurrencyFormatter.formatPlain(m.getGiaTien()));
        cbAvailable.setSelected(m.getConHang() == 1);
        for (int i = 0; i < comboCategory.getItemCount(); i++) {
            if (comboCategory.getItemAt(i).getId() == m.getDanhMucId()) {
                comboCategory.setSelectedIndex(i);
                break;
            }
        }
    }

    private void trySave() {
        errName.setText(" ");
        errPrice.setText(" ");

        boolean ok = true;
        String name = fieldName.getText().trim();
        if (!ValidationUtils.isRequired(name)) {
            errName.setText("Tên món bắt buộc");
            ok = false;
        }
        double price;
        try {
            price = CurrencyFormatter.parse(fieldPrice.getText());
            if (!ValidationUtils.isPositive(price)) {
                errPrice.setText("Giá phải > 0");
                ok = false;
            }
        } catch (NumberFormatException e) {
            errPrice.setText("Giá không hợp lệ");
            ok = false;
            price = 0;
        }
        DanhMuc cat = (DanhMuc) comboCategory.getSelectedItem();
        if (cat == null) {
            errName.setText("Vui lòng chọn danh mục");
            ok = false;
        }
        if (!ok) return;

        MonAn target = editing != null ? editing : new MonAn();
        target.setTenMon(name);
        target.setGiaTien(price);
        target.setDanhMucId(cat.getId());
        target.setConHang(cbAvailable.isSelected() ? 1 : 0);
        onSave.accept(target);
        dispose();
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppFonts.small == null ? l.getFont() : AppFonts.small);
        return l;
    }

    private static JLabel newError() {
        JLabel l = new JLabel(" ");
        l.setForeground(new java.awt.Color(0xEF4444));
        l.setFont(l.getFont().deriveFont(11f));
        l.setPreferredSize(new Dimension(0, 16));
        return l;
    }
}

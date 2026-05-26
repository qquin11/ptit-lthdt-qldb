package com.app.view.common;

import com.app.config.AppColors;
import com.app.config.AppFonts;
import com.app.config.AppSpacing;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 * Modal confirm dialog cho destructive actions (xóa, hủy).
 * Focus default ở nút Hủy (an toàn). Esc = Hủy, Enter = Xác nhận.
 */
public class ConfirmDialog {

    public static boolean ask(java.awt.Component parent, String title, String message) {
        return ask(parent, title, message, "Xác nhận", "Hủy", true);
    }

    public static boolean ask(java.awt.Component parent, String title, String message,
                              String confirmText, String cancelText, boolean destructive) {
        Frame owner = parent == null ? null
                : (Frame) SwingUtilities.getAncestorOfClass(Frame.class, parent);
        JDialog dialog = new JDialog(owner, title, true);
        dialog.setLayout(new BorderLayout());

        JLabel lblMsg = new JLabel("<html><div style='width:280px;'>" + message + "</div></html>");
        lblMsg.setFont(AppFonts.body == null ? lblMsg.getFont() : AppFonts.body);
        lblMsg.setBorder(BorderFactory.createEmptyBorder(
                AppSpacing.XL, AppSpacing.XL, AppSpacing.LG, AppSpacing.XL));
        dialog.add(lblMsg, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppSpacing.SM, AppSpacing.MD));
        actions.setBorder(BorderFactory.createEmptyBorder(0, AppSpacing.LG, AppSpacing.LG, AppSpacing.LG));

        SecondaryButton btnCancel = new SecondaryButton(cancelText);
        PrimaryButton btnConfirm = new PrimaryButton(confirmText);
        if (destructive) {
            btnConfirm.setBackground(AppColors.DANGER);
        }

        final boolean[] result = {false};
        btnCancel.addActionListener(e -> { result[0] = false; dialog.dispose(); });
        btnConfirm.addActionListener(e -> { result[0] = true; dialog.dispose(); });

        actions.add(btnCancel);
        actions.add(btnConfirm);
        dialog.add(actions, BorderLayout.SOUTH);

        // Esc = cancel, Enter = confirm
        JComponent root = (JComponent) dialog.getContentPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "confirm");
        root.getActionMap().put("cancel", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { btnCancel.doClick(); }
        });
        root.getActionMap().put("confirm", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { btnConfirm.doClick(); }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        // Default focus = Cancel (safer)
        SwingUtilities.invokeLater(btnCancel::requestFocusInWindow);
        dialog.setVisible(true);
        return result[0];
    }
}

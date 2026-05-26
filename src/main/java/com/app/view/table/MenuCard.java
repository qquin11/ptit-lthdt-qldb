package com.app.view.table;

import com.app.config.AppColors;
import com.app.config.AppFonts;
import com.app.config.AppSpacing;
import com.app.model.MonAn;
import com.app.util.CurrencyFormatter;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/** Card hiển thị 1 món trong menu — click để add vào order. */
public class MenuCard extends JPanel {

    public MenuCard(MonAn mon, Consumer<MonAn> onClick) {
        super(new BorderLayout(0, AppSpacing.XS));
        setPreferredSize(new Dimension(140, 100));
        setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppColors.LIGHT_BORDER, 1, true),
                BorderFactory.createEmptyBorder(AppSpacing.MD, AppSpacing.MD, AppSpacing.MD, AppSpacing.MD)));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel name = new JLabel("<html><b>" + escape(mon.getTenMon()) + "</b></html>");
        name.setFont(AppFonts.body == null ? name.getFont() : AppFonts.body);

        JLabel price = new JLabel(CurrencyFormatter.format(mon.getGiaTien()), SwingConstants.RIGHT);
        price.setFont(AppFonts.small == null ? price.getFont() : AppFonts.small);
        price.setForeground(AppColors.LIGHT_ACCENT);

        add(name,  BorderLayout.CENTER);
        add(price, BorderLayout.SOUTH);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onClick.accept(mon); }
        });
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;");
    }
}

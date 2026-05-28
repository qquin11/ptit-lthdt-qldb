package com.app.view.main;

import com.app.config.AppColors;
import com.app.config.AppFonts;
import com.app.config.AppSpacing;
import com.app.config.Session;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Bottom status bar 24px — user info (left) + clock (center) + DB status (right). */
public class StatusBar extends JPanel {

    private static final DateTimeFormatter CLOCK_FMT = DateTimeFormatter.ofPattern("HH:mm:ss · dd/MM/yyyy");

    private final JLabel lblUser;
    private final JLabel lblClock;
    private final JLabel lblDb;
    private final Timer ticker;

    public StatusBar() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(100, AppSpacing.H_STATUSBAR));
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
                javax.swing.UIManager.getColor("Component.borderColor")));

        lblUser = newLabel(Session.currentName() + " · " + Session.currentRole(),
                SwingConstants.LEFT);
        lblClock = newLabel(LocalDateTime.now().format(CLOCK_FMT), SwingConstants.CENTER);
        lblDb = newLabel("Online", SwingConstants.RIGHT);
        lblDb.setForeground(AppColors.SUCCESS);

        add(lblUser,  BorderLayout.WEST);
        add(lblClock, BorderLayout.CENTER);
        add(lblDb,    BorderLayout.EAST);

        ticker = new Timer(1000, e -> {
            lblClock.setText(LocalDateTime.now().format(CLOCK_FMT));
        });
        ticker.start();
    }

    public void refreshUser() {
        lblUser.setText(Session.currentName() + " · " + Session.currentRole());
    }

    public void stop() {
        if (ticker != null) ticker.stop();
    }

    private JLabel newLabel(String text, int align) {
        JLabel l = new JLabel(text, align);
        l.setFont(AppFonts.small == null ? l.getFont() : AppFonts.small);
        l.setBorder(BorderFactory.createEmptyBorder(0, AppSpacing.MD, 0, AppSpacing.MD));
        return l;
    }
}

package com.app.view.common;

import com.app.config.AppSpacing;

import javax.swing.JLayeredPane;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Queue & position toasts top-right của parent window. Stack dọc khi có nhiều toast.
 * Auto cleanup khi parent window đóng.
 */
final class ToastManager {

    private static final int MARGIN = AppSpacing.LG;
    private static final int GAP    = AppSpacing.SM;
    private static final Map<JRootPane, Deque<Toast>> ACTIVE = new IdentityHashMap<>();

    private ToastManager() {}

    static void show(Component parent, Toast toast, int durationMs) {
        SwingUtilities.invokeLater(() -> showOnEdt(parent, toast, durationMs));
    }

    private static void showOnEdt(Component parent, Toast toast, int durationMs) {
        JRootPane rootPane = SwingUtilities.getRootPane(parent);
        if (rootPane == null) return;

        JLayeredPane layered = rootPane.getLayeredPane();
        Dimension pref = toast.getPreferredSize();
        Deque<Toast> stack = ACTIVE.computeIfAbsent(rootPane, k -> new ArrayDeque<>());

        int yOffset = MARGIN;
        for (Toast existing : stack) {
            yOffset += existing.getHeight() + GAP;
        }
        int x = layered.getWidth() - pref.width - MARGIN;
        toast.setBounds(x, yOffset, pref.width, pref.height);

        layered.add(toast, JLayeredPane.POPUP_LAYER);
        layered.repaint();
        stack.addLast(toast);

        Timer dismiss = new Timer(durationMs, e -> {
            layered.remove(toast);
            stack.remove(toast);
            layered.repaint();
            // Re-stack remaining toasts
            int y = MARGIN;
            for (Toast t : stack) {
                t.setLocation(t.getX(), y);
                y += t.getHeight() + GAP;
            }
        });
        dismiss.setRepeats(false);
        dismiss.start();
    }
}

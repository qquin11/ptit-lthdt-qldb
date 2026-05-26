package com.app.view.common;

import com.app.config.AppColors;
import com.app.config.AppFonts;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Semi-transparent overlay với spinner + label. Block input vào panel bên dưới.
 *
 * <p>Cách dùng:
 * <pre>{@code
 * LoadingOverlay overlay = new LoadingOverlay();
 * targetPanel.setLayout(new OverlayLayout(targetPanel)); // hoặc add manually
 * targetPanel.add(overlay);
 * overlay.show("Đang tải...");
 * // ... khi xong
 * overlay.hide();
 * }</pre>
 */
public class LoadingOverlay extends JPanel {

    private final JLabel label;
    private final Spinner spinner;
    private final Timer ticker;
    private int tick;

    public LoadingOverlay() {
        super(new BorderLayout());
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        spinner = new Spinner();
        label = new JLabel("Đang tải...", SwingConstants.CENTER);
        label.setFont(AppFonts.body == null ? label.getFont() : AppFonts.body);
        label.setForeground(Color.WHITE);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(spinner, BorderLayout.CENTER);
        center.add(label, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        ticker = new Timer(50, e -> {
            tick = (tick + 1) % 360;
            spinner.repaint();
        });
        setVisible(false);

        // Block clicks
        addMouseListener(new java.awt.event.MouseAdapter() {});
    }

    public void show(String message) {
        if (message != null) label.setText(message);
        setVisible(true);
        ticker.start();
    }

    public void hide() {
        ticker.stop();
        setVisible(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }

    /** Inner spinner — circular arc rotating */
    private class Spinner extends JPanel {
        Spinner() {
            setOpaque(false);
            setPreferredSize(new java.awt.Dimension(48, 48));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new java.awt.BasicStroke(4f, java.awt.BasicStroke.CAP_ROUND,
                    java.awt.BasicStroke.JOIN_ROUND));
            g2.setColor(AppColors.LIGHT_ACCENT);
            int size = Math.min(getWidth(), getHeight()) - 8;
            int x = (getWidth() - size) / 2, y = (getHeight() - size) / 2;
            g2.drawArc(x, y, size, size, tick, 90);
            g2.dispose();
        }
    }
}

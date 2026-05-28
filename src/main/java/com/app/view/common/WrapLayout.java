package com.app.view.common;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

/**
 * FlowLayout biết tự xuống dòng (wrap) khi đặt trong JScrollPane.
 *
 * <p>FlowLayout gốc báo preferred size là 1 hàng dài → trong scroll pane sẽ
 * không wrap mà hiện scrollbar ngang. WrapLayout override preferredLayoutSize
 * để tính chiều cao theo bề rộng thực của container, nên card đủ chỗ thì xuống dòng.
 *
 * <p>Implementation chuẩn của Rob Camick (tips4java).
 */
public class WrapLayout extends FlowLayout {

    public WrapLayout() { super(); }

    public WrapLayout(int align) { super(align); }

    public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        return layoutSize(target, true);
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        Dimension minimum = layoutSize(target, false);
        minimum.width -= (getHgap() + 1);
        return minimum;
    }

    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            int targetWidth = target.getSize().width;
            if (targetWidth == 0) {
                targetWidth = Integer.MAX_VALUE;
            }

            int hgap = getHgap();
            int vgap = getVgap();
            Insets insets = target.getInsets();
            int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
            int maxWidth = targetWidth - horizontalInsetsAndGap;

            Dimension dim = new Dimension(0, 0);
            int rowWidth = 0;
            int rowHeight = 0;

            int nmembers = target.getComponentCount();
            for (int i = 0; i < nmembers; i++) {
                Component m = target.getComponent(i);
                if (!m.isVisible()) continue;
                Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                if (rowWidth + d.width > maxWidth) {
                    addRow(dim, rowWidth, rowHeight, vgap);
                    rowWidth = 0;
                    rowHeight = 0;
                }
                if (rowWidth != 0) {
                    rowWidth += hgap;
                }
                rowWidth += d.width;
                rowHeight = Math.max(rowHeight, d.height);
            }
            addRow(dim, rowWidth, rowHeight, vgap);

            dim.width += horizontalInsetsAndGap;
            dim.height += insets.top + insets.bottom + vgap * 2;

            // Trong scroll pane: giảm width để container co lại đúng, tránh dư scrollbar ngang.
            Container scrollPane = SwingUtilities.getAncestorOfClass(JScrollPane.class, target);
            if (scrollPane != null && target.isValid()) {
                dim.width -= (hgap + 1);
            }
            return dim;
        }
    }

    private void addRow(Dimension dim, int rowWidth, int rowHeight, int vgap) {
        dim.width = Math.max(dim.width, rowWidth);
        if (dim.height > 0) {
            dim.height += vgap;
        }
        dim.height += rowHeight;
    }
}

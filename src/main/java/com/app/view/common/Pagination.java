package com.app.view.common;

import com.app.config.AppFonts;
import com.app.config.AppSpacing;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.util.function.IntConsumer;

/**
 * Pagination control — Prev/Next + page indicator.
 * Caller cấp totalRows, pageSize, gọi {@link #onPageChange(IntConsumer)} để nhận page mới.
 */
public class Pagination extends JPanel {

    private int currentPage = 1;
    private int totalPages = 1;
    private final int pageSize;
    private IntConsumer listener;

    private final GhostButton btnPrev = new GhostButton("‹ Trước");
    private final GhostButton btnNext = new GhostButton("Sau ›");
    private final JLabel lblInfo = new JLabel("");

    public Pagination(int pageSize) {
        super(new FlowLayout(FlowLayout.RIGHT, AppSpacing.SM, AppSpacing.SM));
        this.pageSize = pageSize;
        lblInfo.setFont(AppFonts.small == null ? lblInfo.getFont() : AppFonts.small);

        btnPrev.addActionListener(e -> { if (currentPage > 1) setPage(currentPage - 1); });
        btnNext.addActionListener(e -> { if (currentPage < totalPages) setPage(currentPage + 1); });

        add(lblInfo);
        add(btnPrev);
        add(btnNext);
        updateUi();
    }

    public void setTotalRows(int total) {
        totalPages = Math.max(1, (int) Math.ceil((double) total / pageSize));
        if (currentPage > totalPages) currentPage = totalPages;
        updateUi();
    }

    public void onPageChange(IntConsumer l) {
        this.listener = l;
    }

    public int currentPage() { return currentPage; }
    public int pageSize() { return pageSize; }

    private void setPage(int page) {
        currentPage = page;
        updateUi();
        if (listener != null) listener.accept(currentPage);
    }

    private void updateUi() {
        lblInfo.setText("Trang " + currentPage + " / " + totalPages);
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPages);
    }
}

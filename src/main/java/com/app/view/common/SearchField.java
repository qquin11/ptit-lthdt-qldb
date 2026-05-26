package com.app.view.common;

import com.app.config.AppFonts;
import com.app.config.AppSpacing;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Dimension;
import java.util.function.Consumer;

/**
 * Search input với placeholder + leading icon + clear button (FlatLaf client properties).
 * Dùng realtime filter cho table/list.
 */
public class SearchField extends JTextField {

    public SearchField(String placeholder) {
        super(20);
        setFont(AppFonts.body == null ? getFont() : AppFonts.body);
        setPreferredSize(new Dimension(240, AppSpacing.H_INPUT));
        putClientProperty("JTextField.placeholderText", placeholder);
        putClientProperty("JTextField.showClearButton", true);
        // Leading icon: dùng FlatLaf SVG nếu có (Phase 12 polish)
        // putClientProperty("JTextField.leadingIcon", IconLoader.load("search", 16));
    }

    /** Listener gọi mỗi lần text thay đổi (realtime filter). */
    public void onTextChanged(Consumer<String> listener) {
        getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { listener.accept(getText()); }
            @Override public void removeUpdate(DocumentEvent e) { listener.accept(getText()); }
            @Override public void changedUpdate(DocumentEvent e) { listener.accept(getText()); }
        });
    }
}

package com.app.controller;

import com.app.view.MainFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainController {
    private MainFrame view;

    public MainController(MainFrame view) {
        this.view = view;

        this.view.addClickListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                view.setMessageText("Kết nối Controller thành công!");
            }
        });
    }
}

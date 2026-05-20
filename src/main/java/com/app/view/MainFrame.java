package com.app.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class MainFrame extends JFrame {
    private final JButton btnClickMe;
    private final JLabel lblMessage;

    public MainFrame() {
        setTitle("Java Swing - IntelliJ IDEA");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Giữa màn hình
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 20));

        btnClickMe = new JButton("Click Me!");
        lblMessage = new JLabel("Hệ thống đã sẵn sàng.");

        add(lblMessage);
        add(btnClickMe);
    }

    public void setMessageText(String text) {
        lblMessage.setText(text);
    }

    public void addClickListener(ActionListener listener) {
        btnClickMe.addActionListener(listener);
    }
}

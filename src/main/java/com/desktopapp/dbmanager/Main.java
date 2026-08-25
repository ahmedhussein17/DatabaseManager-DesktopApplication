package com.desktopapp.dbmanager;

import javax.swing.SwingUtilities;

import com.desktopapp.dbmanager.UI.MainFrame;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
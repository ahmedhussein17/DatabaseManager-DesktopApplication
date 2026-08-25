package com.desktopapp.dbmanager.ui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

/**
 * Main application window containing all feature panels inside tabs.
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Database Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Add feature panels to tabbed container[cite: 1]
        tabbedPane.addTab("Connections", new ConnectionPanel());
        tabbedPane.addTab("Backup & Restore", new BackupPanel());
        tabbedPane.addTab("Transfer", new TransferPanel());
        tabbedPane.addTab("DB Objects", new ObjectsPanel());
        tabbedPane.addTab("Operation Logs", new LogsPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}
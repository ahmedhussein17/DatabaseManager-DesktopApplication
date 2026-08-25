package com.desktopapp.dbmanager.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Displays persistent audit and operation logs[cite: 1].
 */
public class LogsPanel extends JPanel {

    private final JTable logsTable;
    private final DefaultTableModel tableModel;
    private final JButton refreshBtn;

    public LogsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Column Setup matching OperationLogger requirements[cite: 1]
        String[] columnNames = {"Date", "Operation", "Source", "Destination", "Result"};
        tableModel = new DefaultTableModel(columnNames, 0);
        logsTable = new JTable(tableModel);

        add(new JScrollPane(logsTable), BorderLayout.CENTER);

        // Control Toolbar
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshBtn = new JButton("Refresh Logs");
        bottomPanel.add(refreshBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> {
            // Hook up to read operations.log or invoke OperationLogger[cite: 1]
        });
    }
}
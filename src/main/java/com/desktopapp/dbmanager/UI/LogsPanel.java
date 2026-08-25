package com.desktopapp.dbmanager.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class LogsPanel extends JPanel {

    private static final String LOG_PATH = "logs/operations.log";
    private static final Pattern LOG_LINE_PATTERN = Pattern.compile(
            "^\\[(.+?)]\\s(.+?)\\s\\|\\ssource=(.*?)\\s\\|\\sdestination=(.*?)\\s\\|\\sresult=(.*)$"
    );

    private final JTable logsTable;
    private final DefaultTableModel tableModel;
    private final JButton refreshBtn;

    public LogsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columnNames = {"Date", "Operation", "Source", "Destination", "Result"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        logsTable = new JTable(tableModel);

        add(new JScrollPane(logsTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshBtn = new JButton("Refresh Logs");
        bottomPanel.add(refreshBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> loadLogs());

        loadLogs();
    }

    private void loadLogs() {
        tableModel.setRowCount(0);

        try (BufferedReader reader = new BufferedReader(new FileReader(LOG_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = LOG_LINE_PATTERN.matcher(line);
                if (matcher.matches()) {
                    tableModel.addRow(new Object[]{
                            matcher.group(1),
                            matcher.group(2),
                            matcher.group(3),
                            matcher.group(4),
                            matcher.group(5)
                    });
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "No logs found yet, or could not read " + LOG_PATH + ": " + e.getMessage(),
                    "Logs", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
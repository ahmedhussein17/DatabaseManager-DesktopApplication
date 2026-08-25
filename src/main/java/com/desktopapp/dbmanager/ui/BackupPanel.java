package com.desktopapp.dbmanager.ui;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Handles single table backups and restorations[cite: 1].
 */
public class BackupPanel extends JPanel {

    private final JComboBox<String> dbComboBox;
    private JTextField tableNameField;
    private final JButton backupBtn;
    private final JButton restoreBtn;

    public BackupPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // DB Environment Selection
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Database Environment:"), gbc);
        
        gbc.gridx = 1;
        dbComboBox = new JComboBox<>(new String[]{"Dev", "Staging", "Production"});
        add(dbComboBox, gbc);

        // Table Name Entry
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Table Name:"), gbc);
        
        gbc.gridx = 1;
        tableNameField = new JTextField(20);
        add(tableNameField, gbc);

        // Action Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        backupBtn = new JButton("Create Backup (TableName_Date)");
        restoreBtn = new JButton("Restore Table");
        btnPanel.add(backupBtn);
        btnPanel.add(restoreBtn);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        add(btnPanel, gbc);

        // Handlers
        backupBtn.addActionListener(e -> {
            // Check if exists and backup via BackupService
            JOptionPane.showMessageDialog(this, "Executing backup for: " + tableNameField.getText());
        });

        restoreBtn.addActionListener(e -> {
            // Call BackupService restore
            JOptionPane.showMessageDialog(this, "Executing restore for: " + tableNameField.getText());
        });
    }
}
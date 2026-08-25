package com.desktopapp.dbmanager.ui;

import com.desktopapp.dbmanager.config.ConfigLoader;
import com.desktopapp.dbmanager.config.Environment;
import com.desktopapp.dbmanager.db.BackupService;
import com.desktopapp.dbmanager.model.OperationResult;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

public class BackupPanel extends JPanel {

    private final ConfigLoader configLoader = new ConfigLoader();
    private final BackupService backupService = new BackupService();

    private final JComboBox<Environment> envComboBox;
    private final JTextField tableNameField;
    private final JTextField backupNameField;
    private final JButton backupBtn;
    private final JButton restoreBtn;

    public BackupPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Database Environment:"), gbc);
        gbc.gridx = 1;
        envComboBox = new JComboBox<>();
        add(envComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Table Name:"), gbc);
        gbc.gridx = 1;
        tableNameField = new JTextField(20);
        add(tableNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Backup Table Name (for restore):"), gbc);
        gbc.gridx = 1;
        backupNameField = new JTextField(20);
        add(backupNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        backupBtn = new JButton("Create Backup (TableName_Date)");
        add(backupBtn, gbc);

        gbc.gridy = 4;
        restoreBtn = new JButton("Restore Table From Backup");
        add(restoreBtn, gbc);

        loadEnvironmentsIntoComboBox();

        backupBtn.addActionListener(e -> runBackup());
        restoreBtn.addActionListener(e -> runRestore());
    }

    private void loadEnvironmentsIntoComboBox() {
        envComboBox.removeAllItems();
        List<Environment> environments = configLoader.loadEnvironments();
        for (Environment env : environments) {
            envComboBox.addItem(env);
        }
    }

    private void runBackup() {
        Environment env = (Environment) envComboBox.getSelectedItem();
        String tableName = tableNameField.getText().trim();

        if (env == null || tableName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select an environment and enter a table name.");
            return;
        }

        setButtonsEnabled(false);
        SwingWorker<OperationResult, Void> worker = new SwingWorker<>() {
            @Override
            protected OperationResult doInBackground() {
                return backupService.backup(env, tableName);
            }

            @Override
            protected void done() {
                try {
                    OperationResult result = get();
                    JOptionPane.showMessageDialog(BackupPanel.this, result.getMessage(),
                            result.isSuccess() ? "Backup Complete" : "Backup Failed",
                            result.isSuccess() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(BackupPanel.this, "Unexpected error: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setButtonsEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void runRestore() {
        Environment env = (Environment) envComboBox.getSelectedItem();
        String tableName = tableNameField.getText().trim();
        String backupName = backupNameField.getText().trim();

        if (env == null || tableName.isEmpty() || backupName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select an environment, and enter both the table name and backup table name.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "This will overwrite " + tableName + " with data from " + backupName + ". Continue?",
                "Confirm Restore", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        setButtonsEnabled(false);
        SwingWorker<OperationResult, Void> worker = new SwingWorker<>() {
            @Override
            protected OperationResult doInBackground() {
                return backupService.restore(env, tableName, backupName);
            }

            @Override
            protected void done() {
                try {
                    OperationResult result = get();
                    JOptionPane.showMessageDialog(BackupPanel.this, result.getMessage(),
                            result.isSuccess() ? "Restore Complete" : "Restore Failed",
                            result.isSuccess() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(BackupPanel.this, "Unexpected error: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setButtonsEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void setButtonsEnabled(boolean enabled) {
        backupBtn.setEnabled(enabled);
        restoreBtn.setEnabled(enabled);
    }
}
package com.desktopapp.dbmanager.ui;

import com.desktopapp.dbmanager.config.ConfigLoader;
import com.desktopapp.dbmanager.config.Environment;
import com.desktopapp.dbmanager.db.BackupService;
import com.desktopapp.dbmanager.db.ObjectService;
import com.desktopapp.dbmanager.model.DbObject;
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
import javax.swing.SwingWorker;

public class BackupPanel extends JPanel {

    private final ConfigLoader configLoader = new ConfigLoader();
    private final ObjectService objectService = new ObjectService();
    private final BackupService backupService = new BackupService();

    private final JComboBox<Environment> envComboBox;
    private final JComboBox<String> tableCombo;
    private final JComboBox<String> backupCombo;
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
        tableCombo = new JComboBox<>();
        add(tableCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Backup Table (for restore):"), gbc);
        gbc.gridx = 1;
        backupCombo = new JComboBox<>();
        add(backupCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        backupBtn = new JButton("Create Backup (TableName_Date)");
        add(backupBtn, gbc);

        gbc.gridy = 4;
        restoreBtn = new JButton("Restore Table From Backup");
        add(restoreBtn, gbc);

        loadEnvironmentsIntoComboBox();

        envComboBox.addActionListener(e -> loadTablesForSelectedEnvironment());
        backupBtn.addActionListener(e -> runBackup());
        restoreBtn.addActionListener(e -> runRestore());
    }

    private void loadEnvironmentsIntoComboBox() {
        envComboBox.removeAllItems();
        List<Environment> environments = configLoader.loadEnvironments();
        for (Environment env : environments) {
            envComboBox.addItem(env);
        }
        loadTablesForSelectedEnvironment();
    }

    private void loadTablesForSelectedEnvironment() {
        Environment env = (Environment) envComboBox.getSelectedItem();
        tableCombo.removeAllItems();
        backupCombo.removeAllItems();
        if (env == null) {
            return;
        }

        setButtonsEnabled(false);
        SwingWorker<List<DbObject>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<DbObject> doInBackground() throws Exception {
                return objectService.listObjects(env);
            }

            @Override
            protected void done() {
                try {
                    List<DbObject> objects = get();
                    for (DbObject obj : objects) {
                        if ("TABLE".equals(obj.getType())) {
                            tableCombo.addItem(obj.getName());
                            backupCombo.addItem(obj.getName());
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(BackupPanel.this,
                            "Failed to load tables for " + env.getName() + ": " + ex.getCause().getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setButtonsEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void runBackup() {
        Environment env = (Environment) envComboBox.getSelectedItem();
        String tableName = (String) tableCombo.getSelectedItem();

        if (env == null || tableName == null) {
            JOptionPane.showMessageDialog(this, "Select an environment and a table.");
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
                    if (result.isSuccess()) {
                        loadTablesForSelectedEnvironment();
                    }
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
        String tableName = (String) tableCombo.getSelectedItem();
        String backupName = (String) backupCombo.getSelectedItem();

        if (env == null || tableName == null || backupName == null) {
            JOptionPane.showMessageDialog(this, "Select an environment, a table, and a backup table.");
            return;
        }

        if (tableName.equals(backupName)) {
            JOptionPane.showMessageDialog(this, "Backup table must be different from the table you're restoring into.");
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
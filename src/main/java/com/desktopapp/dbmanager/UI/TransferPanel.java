package com.desktopapp.dbmanager.ui;

import com.desktopapp.dbmanager.config.ConfigLoader;
import com.desktopapp.dbmanager.config.Environment;
import com.desktopapp.dbmanager.db.TransferService;
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

public class TransferPanel extends JPanel {

    private final ConfigLoader configLoader = new ConfigLoader();
    private final TransferService transferService = new TransferService();

    private final JComboBox<Environment> sourceEnvCombo;
    private final JComboBox<Environment> destEnvCombo;
    private final JTextField objectNameField;
    private final JButton transferBtn;

    public TransferPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Source Environment:"), gbc);
        gbc.gridx = 1;
        sourceEnvCombo = new JComboBox<>();
        add(sourceEnvCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Destination Environment:"), gbc);
        gbc.gridx = 1;
        destEnvCombo = new JComboBox<>();
        add(destEnvCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Table Name:"), gbc);
        gbc.gridx = 1;
        objectNameField = new JTextField(20);
        add(objectNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        transferBtn = new JButton("Transfer (Safe Auto-Backup)");
        add(transferBtn, gbc);

        loadEnvironmentsIntoComboBoxes();

        transferBtn.addActionListener(e -> runTransfer());
    }

    private void loadEnvironmentsIntoComboBoxes() {
        sourceEnvCombo.removeAllItems();
        destEnvCombo.removeAllItems();
        List<Environment> environments = configLoader.loadEnvironments();
        for (Environment env : environments) {
            sourceEnvCombo.addItem(env);
            destEnvCombo.addItem(env);
        }
    }

    private void runTransfer() {
        Environment source = (Environment) sourceEnvCombo.getSelectedItem();
        Environment dest = (Environment) destEnvCombo.getSelectedItem();
        String tableName = objectNameField.getText().trim();

        if (source == null || dest == null || tableName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a source, destination, and enter a table name.");
            return;
        }

        if (source.getName().equals(dest.getName())) {
            JOptionPane.showMessageDialog(this, "Source and destination must be different environments.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Transfer " + tableName + " from " + source.getName() + " to " + dest.getName() +
                ".\nIf it already exists on the destination, it will be backed up first automatically.\nContinue?",
                "Confirm Transfer", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        transferBtn.setEnabled(false);
        SwingWorker<OperationResult, Void> worker = new SwingWorker<>() {
            @Override
            protected OperationResult doInBackground() {
                return transferService.transferTable(source, dest, tableName);
            }

            @Override
            protected void done() {
                try {
                    OperationResult result = get();
                    JOptionPane.showMessageDialog(TransferPanel.this, result.getMessage(),
                            result.isSuccess() ? "Transfer Complete" : "Transfer Failed",
                            result.isSuccess() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(TransferPanel.this, "Unexpected error: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    transferBtn.setEnabled(true);
                }
            }
        };
        worker.execute();
    }
}
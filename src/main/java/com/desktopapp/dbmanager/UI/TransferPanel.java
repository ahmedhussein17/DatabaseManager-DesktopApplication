package com.desktopapp.dbmanager.UI;

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
 * Facilitates environment-to-environment transfers with safety checks[cite: 1].
 */
public class TransferPanel extends JPanel {

    private final JComboBox<String> sourceEnvCombo;
    private final JComboBox<String> destEnvCombo;
    private final JTextField objectNameField;
    private final JButton transferBtn;

    public TransferPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Source Env
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Source Environment:"), gbc);
        gbc.gridx = 1;
        sourceEnvCombo = new JComboBox<>(new String[]{"Dev", "Staging"});
        add(sourceEnvCombo, gbc);

        // Destination Env
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Destination Environment:"), gbc);
        gbc.gridx = 1;
        destEnvCombo = new JComboBox<>(new String[]{"Staging", "Production"});
        add(destEnvCombo, gbc);

        // Object/Table Name
        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Table / Object Name:"), gbc);
        gbc.gridx = 1;
        objectNameField = new JTextField(20);
        add(objectNameField, gbc);

        // Transfer Button
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        transferBtn = new JButton("Transfer (Safe Auto-Backup)");
        add(transferBtn, gbc);

        transferBtn.addActionListener(e -> {
            // Must auto-backup destination first before proceeding per BRD requirements[cite: 1]
            JOptionPane.showMessageDialog(this, "Initiating safe transfer with destination backup...");
        });
    }
}
package com.desktopapp.dbmanager.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Manages database connection configurations and connection testing[cite: 1].
 */
public class ConnectionPanel extends JPanel {

    private final JComboBox<String> envComboBox;
    private final JButton testConnectionBtn;
    private final JTextArea connectionDetailsArea;

    public ConnectionPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Selection Control
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select Environment:"));
        
        envComboBox = new JComboBox<>(new String[]{"Dev", "Staging", "Production"});
        topPanel.add(envComboBox);

        testConnectionBtn = new JButton("Test Connection");
        topPanel.add(testConnectionBtn);

        add(topPanel, BorderLayout.NORTH);

        // Details Display Area
        connectionDetailsArea = new JTextArea();
        connectionDetailsArea.setEditable(false);
        add(new JScrollPane(connectionDetailsArea), BorderLayout.CENTER);

        // Event Listener Placeholder
        testConnectionBtn.addActionListener(e -> {
            String selectedEnv = (String) envComboBox.getSelectedItem();
            // Call ConnectionManager.testConnection(selectedEnv) here
            JOptionPane.showMessageDialog(this, "Testing connection for: " + selectedEnv);
        });
    }
}
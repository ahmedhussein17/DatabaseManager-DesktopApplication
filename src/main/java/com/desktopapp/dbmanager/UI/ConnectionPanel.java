package com.desktopapp.dbmanager.ui;

import com.desktopapp.dbmanager.config.ConfigLoader;
import com.desktopapp.dbmanager.config.Environment;
import com.desktopapp.dbmanager.db.ConnectionManager;
import com.desktopapp.dbmanager.model.OperationResult;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

public class ConnectionPanel extends JPanel {

    private final ConfigLoader configLoader = new ConfigLoader();
    private final ConnectionManager connectionManager = new ConnectionManager();

    private final JComboBox<Environment> envComboBox;
    private final JButton testConnectionBtn;
    private final JButton reloadBtn;
    private final JTextArea connectionDetailsArea;

    public ConnectionPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select Environment:"));

        envComboBox = new JComboBox<>();
        topPanel.add(envComboBox);

        testConnectionBtn = new JButton("Test Connection");
        topPanel.add(testConnectionBtn);

        reloadBtn = new JButton("Reload Connections");
        topPanel.add(reloadBtn);

        add(topPanel, BorderLayout.NORTH);

        connectionDetailsArea = new JTextArea();
        connectionDetailsArea.setEditable(false);
        add(new JScrollPane(connectionDetailsArea), BorderLayout.CENTER);

        loadEnvironmentsIntoComboBox();

        envComboBox.addActionListener(e -> showSelectedEnvironmentDetails());

        testConnectionBtn.addActionListener(e -> testSelectedConnection());

        reloadBtn.addActionListener(e -> {
            loadEnvironmentsIntoComboBox();
            connectionDetailsArea.setText("Connections reloaded from connections.json.");
        });
    }

    private void loadEnvironmentsIntoComboBox() {
        envComboBox.removeAllItems();
        List<Environment> environments = configLoader.loadEnvironments();
        for (Environment env : environments) {
            envComboBox.addItem(env);
        }
        if (!environments.isEmpty()) {
            showSelectedEnvironmentDetails();
        } else {
            connectionDetailsArea.setText("No environments found in config/connections.json.");
        }
    }

    private void showSelectedEnvironmentDetails() {
        Environment env = (Environment) envComboBox.getSelectedItem();
        if (env == null) {
            return;
        }
        connectionDetailsArea.setText(
                "Name: " + env.getName() + "\n" +
                "URL: " + env.getUrl() + "\n" +
                "User: " + env.getUser()
        );
    }

    private void testSelectedConnection() {
        Environment env = (Environment) envComboBox.getSelectedItem();
        if (env == null) {
            connectionDetailsArea.setText("No environment selected.");
            return;
        }

        testConnectionBtn.setEnabled(false);
        connectionDetailsArea.setText("Testing connection to " + env.getName() + "...");

        SwingWorker<OperationResult, Void> worker = new SwingWorker<>() {
            @Override
            protected OperationResult doInBackground() {
                return connectionManager.testConnection(env);
            }

            @Override
            protected void done() {
                try {
                    OperationResult result = get();
                    connectionDetailsArea.setText(
                            "Name: " + env.getName() + "\n" +
                            "URL: " + env.getUrl() + "\n" +
                            "User: " + env.getUser() + "\n\n" +
                            (result.isSuccess() ? "SUCCESS: " : "FAILED: ") + result.getMessage()
                    );
                } catch (Exception ex) {
                    connectionDetailsArea.setText("Unexpected error: " + ex.getMessage());
                } finally {
                    testConnectionBtn.setEnabled(true);
                }
            }
        };
        worker.execute();
    }
}
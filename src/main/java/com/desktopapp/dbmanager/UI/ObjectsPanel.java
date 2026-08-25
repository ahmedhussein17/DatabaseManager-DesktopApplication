package com.desktopapp.dbmanager.ui;

import com.desktopapp.dbmanager.config.ConfigLoader;
import com.desktopapp.dbmanager.config.Environment;
import com.desktopapp.dbmanager.db.ObjectService;
import com.desktopapp.dbmanager.model.DbObject;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionListener;

public class ObjectsPanel extends JPanel {

    private final ConfigLoader configLoader = new ConfigLoader();
    private final ObjectService objectService = new ObjectService();

    private final JComboBox<Environment> envCombo;
    private final JComboBox<String> filterTypeCombo;
    private final JButton refreshBtn;
    private final DefaultListModel<DbObject> listModel;
    private final JList<DbObject> objectsList;
    private final JTextArea definitionArea;

    public ObjectsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Environment:"));
        envCombo = new JComboBox<>();
        topPanel.add(envCombo);

        topPanel.add(new JLabel("Filter Object Type:"));
        filterTypeCombo = new JComboBox<>(new String[]{"ALL", "TABLE", "VIEW", "PROCEDURE"});
        topPanel.add(filterTypeCombo);

        refreshBtn = new JButton("Refresh");
        topPanel.add(refreshBtn);

        add(topPanel, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        objectsList = new JList<>(listModel);
        definitionArea = new JTextArea("Select an object to view its definition.");
        definitionArea.setEditable(false);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(objectsList),
                new JScrollPane(definitionArea)
        );
        splitPane.setDividerLocation(250);

        add(splitPane, BorderLayout.CENTER);

        loadEnvironmentsIntoComboBox();

        refreshBtn.addActionListener(e -> loadObjects());
        filterTypeCombo.addActionListener(e -> applyFilterAndRedisplay());
        objectsList.addListSelectionListener(makeSelectionListener());
    }

    private void loadEnvironmentsIntoComboBox() {
        envCombo.removeAllItems();
        List<Environment> environments = configLoader.loadEnvironments();
        for (Environment env : environments) {
            envCombo.addItem(env);
        }
    }

    private List<DbObject> allObjects = new ArrayList<>();

    private void loadObjects() {
        Environment env = (Environment) envCombo.getSelectedItem();
        if (env == null) {
            JOptionPane.showMessageDialog(this, "Select an environment first.");
            return;
        }

        refreshBtn.setEnabled(false);
        listModel.clear();
        definitionArea.setText("Loading...");

        SwingWorker<List<DbObject>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<DbObject> doInBackground() throws Exception {
                return objectService.listObjects(env);
            }

            @Override
            protected void done() {
                try {
                    allObjects = get();
                    applyFilterAndRedisplay();
                    definitionArea.setText("Select an object to view its definition.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ObjectsPanel.this,
                            "Failed to load objects: " + ex.getCause().getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    refreshBtn.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void applyFilterAndRedisplay() {
        String filter = (String) filterTypeCombo.getSelectedItem();
        listModel.clear();
        for (DbObject obj : allObjects) {
            if ("ALL".equals(filter) || obj.getType().equals(filter)) {
                listModel.addElement(obj);
            }
        }
    }

    private ListSelectionListener makeSelectionListener() {
        return e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            DbObject selected = objectsList.getSelectedValue();
            Environment env = (Environment) envCombo.getSelectedItem();
            if (selected == null || env == null) {
                return;
            }

            definitionArea.setText("Loading definition...");
            SwingWorker<String, Void> worker = new SwingWorker<>() {
                @Override
                protected String doInBackground() throws Exception {
                    return objectService.getDefinition(env, selected.getName());
                }

                @Override
                protected void done() {
                    try {
                        definitionArea.setText(get());
                    } catch (Exception ex) {
                        definitionArea.setText("Failed to load definition: " + ex.getCause().getMessage());
                    }
                }
            };
            worker.execute();
        };
    }
}
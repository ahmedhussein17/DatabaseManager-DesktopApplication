package com.desktopapp.dbmanager.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

/**
 * Displays Tables, Views, Stored Procedures and their definitions[cite: 1].
 */
public class ObjectsPanel extends JPanel {

    private  final JComboBox<String> filterTypeCombo;
    private final JList<String> objectsList;
    private final JTextArea definitionArea;

    public ObjectsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Filter Bar
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Filter Object Type:"));
        filterTypeCombo = new JComboBox<>(new String[]{"TABLE", "VIEW", "PROCEDURE"});
        topPanel.add(filterTypeCombo);
        add(topPanel, BorderLayout.NORTH);

        // Split Pane: Left (Object List), Right (Definition View)
        DefaultListModel<String> listModel = new DefaultListModel<>();
        listModel.addElement("Sample_Table_1");
        listModel.addElement("Sample_View_1");
        listModel.addElement("Sample_Procedure_1");

        objectsList = new JList<>(listModel);
        definitionArea = new JTextArea("SELECT * FROM definition_placeholder;");
        definitionArea.setEditable(false);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(objectsList),
                new JScrollPane(definitionArea)
        );
        splitPane.setDividerLocation(250);

        add(splitPane, BorderLayout.CENTER);
    }
}
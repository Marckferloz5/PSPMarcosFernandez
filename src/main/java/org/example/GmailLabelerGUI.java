package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class GmailLabelerGUI extends JFrame {
    private final GmailLabeler labeler = new GmailLabeler();

    public GmailLabelerGUI() {
        setTitle("Etiquetador de Gmail");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        initUI();
        setLocationRelativeTo(null);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        SwingWorker<Map<String, List<String>>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<String, List<String>> doInBackground() {
                labeler.labelEmails();
                return labeler.fetchLabeledMessages();
            }

            @Override
            protected void done() {
                try {
                    Map<String, List<String>> messages = get();
                    addLabelPanels(mainPanel, messages);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(GmailLabelerGUI.this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();

        add(new JScrollPane(mainPanel));
    }

    private void addLabelPanels(JPanel mainPanel, Map<String, List<String>> messages) {
        for (Map.Entry<String, List<String>> entry : messages.entrySet()) {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createTitledBorder(entry.getKey()));

            DefaultListModel<String> listModel = new DefaultListModel<>();
            entry.getValue().forEach(listModel::addElement);

            JList<String> list = new JList<>(listModel);
            panel.add(new JScrollPane(list), BorderLayout.CENTER);
            panel.add(new JLabel("Total: " + entry.getValue().size()), BorderLayout.NORTH);

            mainPanel.add(panel);
        }
        revalidate();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new GmailLabelerGUI().setVisible(true));
    }
}
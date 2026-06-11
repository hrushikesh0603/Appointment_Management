package com.appointmentmanager.view;

import com.appointmentmanager.dao.FeedbackDAO;
import com.appointmentmanager.model.Appointment;
import com.appointmentmanager.model.Feedback;
import com.appointmentmanager.util.DateTimeUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class FeedbackPanel extends JPanel {

    private final FeedbackDAO feedbackDAO;
    private JTable feedbackTable;
    private DefaultTableModel tableModel;

private static final Color BG_LIGHT = new Color(245, 245, 247);
    private static final Color BORDER_LIGHT = new Color(220, 220, 225);
    private static final Color ACCENT_BLUE = new Color(37, 99, 235);
    private static final Color ACCENT_GREEN = new Color(22, 163, 74);
    private static final Color ACCENT_GOLD = new Color(217, 119, 6);

    public FeedbackPanel() {
        this.feedbackDAO = new FeedbackDAO();
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(BG_LIGHT);

        initComponents();
        refreshFeedbacks();
    }

    private void initComponents() {
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Client Feedback");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerPanel.add(titleLabel, BorderLayout.WEST);

JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        JButton recordBtn = createActionButton("Record Feedback", ACCENT_GREEN);
        recordBtn.addActionListener(e -> openRecordFeedbackDialog());
        actionPanel.add(recordBtn);

        headerPanel.add(actionPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

String[] columnNames = {"ID", "Client Name", "Appointment Date", "Comments", "Date Logged"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        feedbackTable = new JTable(tableModel);
        feedbackTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        feedbackTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        feedbackTable.setRowHeight(32);
        feedbackTable.setGridColor(BORDER_LIGHT);
        feedbackTable.setShowGrid(true);
        feedbackTable.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = feedbackTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(0, 36));

feedbackTable.getColumnModel().getColumn(0).setPreferredWidth(50);   
        feedbackTable.getColumnModel().getColumn(1).setPreferredWidth(180);  
        feedbackTable.getColumnModel().getColumn(2).setPreferredWidth(130);  
        feedbackTable.getColumnModel().getColumn(3).setPreferredWidth(470);  
        feedbackTable.getColumnModel().getColumn(4).setPreferredWidth(170);  

DefaultTableCellRenderer regularRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
                    c.setForeground(Color.BLACK);
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        };

        for (int i = 0; i < feedbackTable.getColumnCount(); i++) {
            feedbackTable.getColumnModel().getColumn(i).setCellRenderer(regularRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(feedbackTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshFeedbacks() {
        try {
            tableModel.setRowCount(0);
            List<Feedback> feedbacks = feedbackDAO.getAllFeedbacks();
            for (Feedback f : feedbacks) {
                tableModel.addRow(new Object[]{
                        f.getId(),
                        f.getClientName(),
                        f.getAppointmentDate(),
                        f.getComments() != null ? f.getComments() : "",
                        f.getCreatedAt()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading feedback list: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openRecordFeedbackDialog() {
        try {
            List<Appointment> completedNoFeedback = feedbackDAO.getCompletedAppointmentsWithoutFeedback();
            if (completedNoFeedback.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No completed appointments available for logging feedback.", "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Record Client Feedback", true);
            dialog.setLayout(new BorderLayout(10, 10));
            dialog.setSize(480, 280);
            dialog.setLocationRelativeTo(this);

            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 8, 8, 8);
            gbc.fill = GridBagConstraints.HORIZONTAL;

gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("Select Visit:"), gbc);

            JComboBox<AppointmentComboItem> comboAppointments = new JComboBox<>();
            for (Appointment a : completedNoFeedback) {
                comboAppointments.addItem(new AppointmentComboItem(a));
            }
            gbc.gridx = 1;
            gbc.weightx = 1.0;
            formPanel.add(comboAppointments, gbc);

gbc.gridx = 0; gbc.gridy = 1;
            gbc.weightx = 0.0;
            formPanel.add(new JLabel("Comments:"), gbc);

            JTextArea txtComments = new JTextArea(4, 25);
            txtComments.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            txtComments.setLineWrap(true);
            txtComments.setWrapStyleWord(true);
            JScrollPane scrollComments = new JScrollPane(txtComments);
            gbc.gridx = 1;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            formPanel.add(scrollComments, gbc);

            dialog.add(formPanel, BorderLayout.CENTER);

JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            JButton btnSave = createActionButton("Save", ACCENT_GREEN);
            JButton btnCancel = createActionButton("Cancel", null);

            btnCancel.addActionListener(e -> dialog.dispose());
            btnSave.addActionListener(e -> {
                AppointmentComboItem selectedItem = (AppointmentComboItem) comboAppointments.getSelectedItem();
                if (selectedItem == null) {
                    JOptionPane.showMessageDialog(dialog, "Please select an appointment.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Appointment appt = selectedItem.getAppointment();
                int rating = 5; 
                String comments = txtComments.getText().trim();

                try {
                    Feedback feedback = new Feedback(appt.getId(), appt.getClientId(), rating, comments);
                    int feedbackId = feedbackDAO.insertFeedback(feedback);
                    if (feedbackId > 0) {
                        dialog.dispose();
                        JOptionPane.showMessageDialog(this, "Feedback recorded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        refreshFeedbacks();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Failed to record feedback.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            buttonPanel.add(btnCancel);
            buttonPanel.add(btnSave);
            dialog.add(buttonPanel, BorderLayout.SOUTH);

            dialog.setVisible(true);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error querying database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton createActionButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        if (bgColor != null) {
            button.setBackground(bgColor);
            button.setForeground(Color.WHITE);
            button.putClientProperty("JButton.buttonType", "primary");
        }
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 32));
        return button;
    }

private static class AppointmentComboItem {
        private final Appointment appointment;

        public AppointmentComboItem(Appointment appointment) {
            this.appointment = appointment;
        }

        public Appointment getAppointment() {
            return appointment;
        }

        @Override
        public String toString() {
            String service = appointment.getServiceType();
            String date = DateTimeUtil.formatDateForDisplay(appointment.getAppointmentDate());
            return "ID " + appointment.getId() + " - " + appointment.getClientName() + " (" + service + " on " + date + ")";
        }
    }
}

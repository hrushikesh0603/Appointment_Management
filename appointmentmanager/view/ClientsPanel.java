package com.appointmentmanager.view;

import com.appointmentmanager.dao.ClientDAO;
import com.appointmentmanager.model.Appointment;
import com.appointmentmanager.model.Client;
import com.appointmentmanager.util.DateTimeUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ClientsPanel extends JPanel {

    private final ClientDAO clientDAO;
    private JTable clientsTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JButton btnSearch, btnDelete, btnHistory;

    public ClientsPanel() {
        this.clientDAO = new ClientDAO();
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initComponents();
        loadClients();
    }

    private void initComponents() {
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Clients Directory");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerPanel.add(titleLabel, BorderLayout.WEST);

JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

txtSearch = new JTextField(15);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search by name or phone...");
        btnSearch = new JButton("🔍");
        btnSearch.addActionListener(e -> searchClients());
        txtSearch.addActionListener(e -> searchClients());

        btnHistory = new JButton("📋 View History");
        btnDelete = new JButton("❌ Delete");

        btnHistory.addActionListener(e -> viewClientHistory());
        btnDelete.addActionListener(e -> deleteSelectedClient());

        actionPanel.add(txtSearch);
        actionPanel.add(btnSearch);
        actionPanel.add(btnHistory);
        actionPanel.add(btnDelete);
        headerPanel.add(actionPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

String[] columnNames = {"ID", "Name", "Phone", "Email"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Integer.class;
                }
                return String.class;
            }
        };

        clientsTable = new JTable(tableModel);
        clientsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clientsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        clientsTable.setRowHeight(25);

        javax.swing.table.TableRowSorter<DefaultTableModel> sorter = new javax.swing.table.TableRowSorter<>(tableModel);
        sorter.setComparator(0, (o1, o2) -> Integer.compare((Integer) o1, (Integer) o2));
        java.util.List<javax.swing.RowSorter.SortKey> sortKeys = new java.util.ArrayList<>();
        sortKeys.add(new javax.swing.RowSorter.SortKey(0, javax.swing.SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
        sorter.setSortsOnUpdates(true);
        clientsTable.setRowSorter(sorter);

        clientsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        clientsTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        clientsTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        clientsTable.getColumnModel().getColumn(3).setPreferredWidth(250);

        JScrollPane scrollPane = new JScrollPane(clientsTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadClients() {
        try {
            tableModel.setRowCount(0);
            List<Client> clients = clientDAO.getAllClients();
            for (Client c : clients) {
                tableModel.addRow(new Object[]{
                        c.getId(),
                        c.getName(),
                        c.getPhone(),
                        c.getEmail() != null ? c.getEmail() : ""
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading clients: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchClients() {
        String query = txtSearch.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            loadClients();
            return;
        }

        try {
            tableModel.setRowCount(0);
            List<Client> clients = clientDAO.getAllClients();
            for (Client c : clients) {
                if (c.getName().toLowerCase().contains(query) || c.getPhone().contains(query) || 
                   (c.getEmail() != null && c.getEmail().toLowerCase().contains(query))) {
                    tableModel.addRow(new Object[]{
                            c.getId(),
                            c.getName(),
                            c.getPhone(),
                            c.getEmail() != null ? c.getEmail() : ""
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error searching clients: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

private void deleteSelectedClient() {
        int selectedRow = clientsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a client to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = clientsTable.convertRowIndexToModel(selectedRow);
        int id = (int) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the client profile: " + name + "?\nAppointments referencing this client will remain but their link will be set to NULL.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (clientDAO.deleteClient(id)) {
                    loadClients();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting client: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewClientHistory() {
        int selectedRow = clientsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a client to view their booking history.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = clientsTable.convertRowIndexToModel(selectedRow);
        int id = (int) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);

        try {
            List<Appointment> history = clientDAO.getClientAppointmentHistory(id);

            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Booking History - " + name, true);
            dialog.setLayout(new BorderLayout(10, 10));
            dialog.setSize(650, 400);
            dialog.setLocationRelativeTo(this);

            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

            JLabel titleLabel = new JLabel("📅 Booking History for " + name);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
            mainPanel.add(titleLabel, BorderLayout.NORTH);

            String[] columnNames = {"Date", "Time", "Service", "Staff", "Remarks"};
            DefaultTableModel histModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            for (Appointment a : history) {
                histModel.addRow(new Object[]{
                        DateTimeUtil.formatDateForDisplay(a.getAppointmentDate()),
                        DateTimeUtil.formatTime(a.getAppointmentTime()),
                        a.getServiceType(),
                        a.getAssignedStaff(),
                        a.getRemarks() != null ? a.getRemarks() : ""
                });
            }

            JTable histTable = new JTable(histModel);
            histTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            histTable.setRowHeight(22);
            JScrollPane scrollPane = new JScrollPane(histTable);
            mainPanel.add(scrollPane, BorderLayout.CENTER);

            JButton btnClose = new JButton("Close");
            btnClose.addActionListener(e -> dialog.dispose());
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            btnPanel.add(btnClose);
            mainPanel.add(btnPanel, BorderLayout.SOUTH);

            dialog.add(mainPanel);
            dialog.setVisible(true);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching client history: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}

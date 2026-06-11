package com.appointmentmanager.view;

import com.appointmentmanager.dao.ServiceDAO;
import com.appointmentmanager.model.Service;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServicesPanel extends JPanel {

    private final ServiceDAO serviceDAO;
    private JTable servicesTable;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnEdit, btnDelete;

    public ServicesPanel() {
        this.serviceDAO = new ServiceDAO();
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        initComponents();
        loadServices();
    }

    private void initComponents() {
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("🏥 Services Catalog");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerPanel.add(titleLabel, BorderLayout.WEST);

JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);
        
        btnAdd = new JButton("➕ Add Service");
        btnEdit = new JButton("✏️ Edit");
        btnDelete = new JButton("❌ Delete");
        
        btnAdd.addActionListener(e -> openServiceDialog(null));
        btnEdit.addActionListener(e -> editSelectedService());
        btnDelete.addActionListener(e -> deleteSelectedService());
        
        actionPanel.add(btnAdd);
        actionPanel.add(btnEdit);
        actionPanel.add(btnDelete);
        headerPanel.add(actionPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

String[] columnNames = {"ID", "Name", "Description", "Price (₹)", "Duration (mins)"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };

        servicesTable = new JTable(tableModel);
        servicesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        servicesTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        servicesTable.setRowHeight(25);

TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        sorter.setComparator(0, (o1, o2) -> Integer.compare((Integer) o1, (Integer) o2));
        sorter.setComparator(3, (o1, o2) -> {
            try {
                double d1 = Double.parseDouble((String) o1);
                double d2 = Double.parseDouble((String) o2);
                return Double.compare(d1, d2);
            } catch (Exception e) {
                return 0;
            }
        });
        sorter.setComparator(4, (o1, o2) -> Integer.compare((Integer) o1, (Integer) o2));

        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(3, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
        sorter.setSortsOnUpdates(true);
        servicesTable.setRowSorter(sorter);

servicesTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        servicesTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        servicesTable.getColumnModel().getColumn(2).setPreferredWidth(300);
        servicesTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        servicesTable.getColumnModel().getColumn(4).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(servicesTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadServices() {
        try {
            tableModel.setRowCount(0);
            List<Service> services = serviceDAO.getAllServices();
            for (Service s : services) {
                tableModel.addRow(new Object[]{
                    s.getId(),
                    s.getName(),
                    s.getDescription(),
                    String.format("%.2f", s.getPrice()),
                    s.getDurationMinutes()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading services: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editSelectedService() {
        int selectedRow = servicesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a service to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        try {
            Service s = serviceDAO.getServiceById(id);
            if (s != null) {
                openServiceDialog(s);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching service details: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedService() {
        int selectedRow = servicesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a service to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete the service: " + name + "?\nAppointments referencing this service will remain but link will be set to NULL.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (serviceDAO.deleteService(id)) {
                    loadServices();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting service: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openServiceDialog(Service existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            existing == null ? "Add Service" : "Edit Service", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

JTextField txtName = new JTextField(20);
        JTextField txtDesc = new JTextField(20);
        JSpinner spinPrice = new JSpinner(new SpinnerNumberModel(0.00, 0.00, 100000.00, 50.00));
        JSpinner spinDuration = new JSpinner(new SpinnerNumberModel(60, 5, 480, 5));

        if (existing != null) {
            txtName.setText(existing.getName());
            txtDesc.setText(existing.getDescription());
            spinPrice.setValue(existing.getPrice());
            spinDuration.setValue(existing.getDurationMinutes());
        }

gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Name *:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtDesc, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Price (₹) *:"), gbc);
        gbc.gridx = 1;
        formPanel.add(spinPrice, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Duration (mins) *:"), gbc);
        gbc.gridx = 1;
        formPanel.add(spinDuration, gbc);

        dialog.add(formPanel, BorderLayout.CENTER);

JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String name = txtName.getText().trim();
            String desc = txtDesc.getText().trim();
            double price = (double) spinPrice.getValue();
            int duration = (int) spinDuration.getValue();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Name is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                
                Service sByName = serviceDAO.getServiceByName(name);
                if (sByName != null && (existing == null || sByName.getId() != existing.getId())) {
                    JOptionPane.showMessageDialog(dialog, "A service with this name already exists.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (existing == null) {
                    Service newService = new Service(name, desc, price, duration);
                    serviceDAO.insertService(newService);
                } else {
                    existing.setName(name);
                    existing.setDescription(desc);
                    existing.setPrice(price);
                    existing.setDurationMinutes(duration);
                    serviceDAO.updateService(existing);
                }

                dialog.dispose();
                loadServices();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error saving service: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}

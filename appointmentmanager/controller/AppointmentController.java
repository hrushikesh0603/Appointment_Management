package com.appointmentmanager.controller;

import com.appointmentmanager.model.Appointment;
import com.appointmentmanager.service.AppointmentService;
import com.appointmentmanager.util.DateTimeUtil;
import com.appointmentmanager.util.ExportUtil;
import com.appointmentmanager.view.AppointmentDialog;
import com.appointmentmanager.view.MainFrame;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class AppointmentController {

    private final MainFrame mainFrame;
    private final AppointmentService appointmentService;

    public AppointmentController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.appointmentService = new AppointmentService();
    }

public void refreshTable() {
        SwingWorker<List<Appointment>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Appointment> doInBackground() throws Exception {
                return appointmentService.getAllAppointments();
            }

            @Override
            protected void done() {
                try {
                    List<Appointment> appointments = get();
                    mainFrame.updateTable(appointments);
                } catch (Exception e) {
                    mainFrame.showError("Failed to load appointments: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

public void showAddDialog() {
        showAddDialog(null, null, null);
    }

public void showAddDialog(String defaultStaff, LocalDate defaultDate, LocalTime defaultTime) {
        AppointmentDialog dialog = new AppointmentDialog(mainFrame, defaultStaff, defaultDate, defaultTime);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            Appointment appointment = dialog.getAppointment();
            SwingWorker<Integer, Void> worker = new SwingWorker<>() {
                @Override
                protected Integer doInBackground() throws Exception {
                    return appointmentService.addAppointment(appointment);
                }

                @Override
                protected void done() {
                    try {
                        int id = get();
                        if (id > 0) {
                            mainFrame.showSuccess("Appointment created successfully! (ID: " + id + ")");
                            refreshTable();
                            if (mainFrame.getStaffAvailabilityPanel() != null) {
                                mainFrame.getStaffAvailabilityPanel().refreshAvailability();
                            }
                            if (mainFrame.getClientsPanel() != null) {
                                mainFrame.getClientsPanel().loadClients();
                            }
                        } else {
                            mainFrame.showError("Failed to create appointment.");
                        }
                    } catch (Exception e) {
                        String message = extractErrorMessage(e);
                        mainFrame.showError("Failed to create appointment:\n" + message);
                        e.printStackTrace();
                    }
                }
            };
            worker.execute();
        }
    }

public void showEditDialog() {
        int selectedId = mainFrame.getSelectedAppointmentId();
        if (selectedId == -1) {
            mainFrame.showError("Please select an appointment to edit.");
            return;
        }

        SwingWorker<Appointment, Void> worker = new SwingWorker<>() {
            @Override
            protected Appointment doInBackground() throws Exception {
                return appointmentService.getAppointmentById(selectedId);
            }

            @Override
            protected void done() {
                try {
                    Appointment existing = get();
                    if (existing == null) {
                        mainFrame.showError("Appointment not found.");
                        return;
                    }

                    AppointmentDialog dialog = new AppointmentDialog(mainFrame, existing);
                    dialog.setVisible(true);

                    if (dialog.isConfirmed()) {
                        Appointment updated = dialog.getAppointment();
                        updateAppointment(updated);
                    }
                } catch (Exception e) {
                    mainFrame.showError("Failed to load appointment: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

private void updateAppointment(Appointment appointment) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return appointmentService.updateAppointment(appointment);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        mainFrame.showSuccess("Appointment updated successfully!");
                        refreshTable();
                        if (mainFrame.getClientsPanel() != null) {
                            mainFrame.getClientsPanel().loadClients();
                        }
                    } else {
                        mainFrame.showError("Failed to update appointment.");
                    }
                } catch (Exception e) {
                    String message = extractErrorMessage(e);
                    mainFrame.showError("Failed to update appointment:\n" + message);
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

public void cancelAppointment() {
        int selectedId = mainFrame.getSelectedAppointmentId();
        if (selectedId == -1) {
            mainFrame.showError("Please select an appointment to cancel.");
            return;
        }

        if (!mainFrame.showConfirm("Are you sure you want to cancel appointment #" + selectedId + "?\nThis action cannot be undone.")) {
            return;
        }

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return appointmentService.cancelAppointment(selectedId);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        mainFrame.showSuccess("Appointment #" + selectedId + " has been cancelled.");
                        refreshTable();
                    } else {
                        mainFrame.showError("Failed to cancel appointment.");
                    }
                } catch (Exception e) {
                    mainFrame.showError("Failed to cancel appointment: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

public void searchAppointments() {
        String searchText = mainFrame.getSearchText();
        String searchField = mainFrame.getSearchField();

        if (searchText.isEmpty()) {
            refreshTable();
            return;
        }

        SwingWorker<List<Appointment>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Appointment> doInBackground() throws Exception {
                return appointmentService.searchAppointments(searchField, searchText);
            }

            @Override
            protected void done() {
                try {
                    List<Appointment> results = get();
                    mainFrame.updateTable(results);
                } catch (Exception e) {
                    mainFrame.showError("Search failed: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

public void filterByDate(LocalDate date) {
        SwingWorker<List<Appointment>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Appointment> doInBackground() throws Exception {
                return appointmentService.searchAppointments("appointment_date",
                        DateTimeUtil.formatDate(date));
            }

            @Override
            protected void done() {
                try {
                    List<Appointment> results = get();
                    mainFrame.updateTable(results);
                } catch (Exception e) {
                    mainFrame.showError("Filter failed: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

public void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export to CSV");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
        fileChooser.setSelectedFile(new File("appointments_" +
                DateTimeUtil.formatDate(LocalDate.now()) + ".csv"));

        if (fileChooser.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().endsWith(".csv")) {
                file = new File(file.getAbsolutePath() + ".csv");
            }

            File finalFile = file;
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    List<Appointment> appointments = appointmentService.getAllAppointments();
                    ExportUtil.exportToCSV(appointments, finalFile.getAbsolutePath());
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        mainFrame.showSuccess("Data exported successfully to:\n" + finalFile.getAbsolutePath());
                    } catch (Exception e) {
                        mainFrame.showError("Export failed: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            };
            worker.execute();
        }
    }

public void exportToPDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export to PDF");
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Files (*.pdf)", "pdf"));
        fileChooser.setSelectedFile(new File("appointments_" +
                DateTimeUtil.formatDate(LocalDate.now()) + ".pdf"));

        if (fileChooser.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().endsWith(".pdf")) {
                file = new File(file.getAbsolutePath() + ".pdf");
            }

            File finalFile = file;
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    List<Appointment> appointments = appointmentService.getAllAppointments();
                    ExportUtil.exportToPDF(appointments, finalFile.getAbsolutePath());
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        mainFrame.showSuccess("Data exported successfully to:\n" + finalFile.getAbsolutePath());
                    } catch (Exception e) {
                        mainFrame.showError("Export failed: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            };
            worker.execute();
        }
    }

private String extractErrorMessage(Exception e) {
        Throwable cause = e.getCause();
        if (cause != null) {
            if (cause instanceof IllegalStateException || cause instanceof IllegalArgumentException) {
                return cause.getMessage();
            }
            if (cause instanceof SQLException) {
                String sqlMsg = cause.getMessage();
                if (sqlMsg.contains("Duplicate entry") || sqlMsg.contains("uk_staff_datetime")) {
                    return "Scheduling conflict: This staff member already has an appointment at this date and time.";
                }
                return "Database error: " + sqlMsg;
            }
        }
        return e.getMessage();
    }
}
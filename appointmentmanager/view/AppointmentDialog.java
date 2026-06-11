package com.appointmentmanager.view;

import com.appointmentmanager.model.Appointment;
import com.appointmentmanager.service.AppointmentService;
import com.appointmentmanager.util.DateTimeUtil;
import com.appointmentmanager.util.ValidationUtil;
import com.appointmentmanager.dao.ServiceDAO;
import com.appointmentmanager.dao.ClientDAO;
import com.appointmentmanager.model.Service;
import com.appointmentmanager.model.Client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AppointmentDialog extends JDialog {

    private JTextField clientNameField;
    private JTextField contactNumberField;
    private JTextField clientEmailField;
    private com.toedter.calendar.JDateChooser dateChooser;
    private JSpinner timeSpinner;
    private JComboBox<String> serviceTypeCombo;
    private JComboBox<String> assignedStaffCombo;
    private JTextArea remarksArea;

    private boolean confirmed = false;
    private Appointment appointment;
    private final boolean isEditMode;

    private static final Color BORDER_LIGHT = new Color(220, 220, 225);
    private static final Color ACCENT_BLUE = new Color(37, 99, 235);
    private static final Color ERROR_BORDER = new Color(220, 38, 38);

    public AppointmentDialog(JFrame parent) {
        super(parent, "Add New Appointment", true);
        this.isEditMode = false;
        this.appointment = new Appointment();
        initializeUI();
    }

    public AppointmentDialog(JFrame parent, String defaultStaff, LocalDate defaultDate, LocalTime defaultTime) {
        super(parent, "Add New Appointment", true);
        this.isEditMode = false;
        this.appointment = new Appointment();
        initializeUI();

        if (defaultStaff != null) {
            assignedStaffCombo.setSelectedItem(defaultStaff);
        }
        if (defaultDate != null) {
            dateChooser.setDate(Date.from(defaultDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        if (defaultTime != null) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, defaultTime.getHour());
            cal.set(Calendar.MINUTE, defaultTime.getMinute());
            cal.set(Calendar.SECOND, 0);
            timeSpinner.setValue(cal.getTime());
        }
    }

    public AppointmentDialog(JFrame parent, Appointment existingAppointment) {
        super(parent, "Edit Appointment #" + existingAppointment.getId(), true);
        this.isEditMode = true;
        this.appointment = existingAppointment;
        initializeUI();
        populateFields(existingAppointment);
    }

    private void initializeUI() {
        setSize(500, 720); 
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout());

JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_LIGHT));

        JLabel headerIcon = new JLabel(isEditMode ? "✏️" : "➕");
        headerIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        headerPanel.add(headerIcon);

        JLabel headerLabel = new JLabel(isEditMode ? "Edit Appointment" : "New Appointment");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);

JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(15, 30, 10, 30));

formPanel.add(createFieldLabel("Contact Phone *"));
        contactNumberField = createStyledTextField();
        contactNumberField.putClientProperty("JTextField.placeholderText", "Enter 10-digit phone number");
        formPanel.add(contactNumberField);
        formPanel.add(Box.createVerticalStrut(10));

contactNumberField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String phone = contactNumberField.getText().trim();
                if (phone.length() >= 10) {
                    try {
                        Client client = new ClientDAO().getClientByPhone(phone);
                        if (client != null) {
                            if (clientNameField.getText().trim().isEmpty()) {
                                clientNameField.setText(client.getName());
                            }
                            if (clientEmailField.getText().trim().isEmpty()) {
                                clientEmailField.setText(client.getEmail() != null ? client.getEmail() : "");
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

formPanel.add(createFieldLabel("Client Name *"));
        clientNameField = createStyledTextField();
        formPanel.add(clientNameField);
        formPanel.add(Box.createVerticalStrut(10));

formPanel.add(createFieldLabel("Client Email *"));
        clientEmailField = createStyledTextField();
        clientEmailField.putClientProperty("JTextField.placeholderText", "example@email.com");
        formPanel.add(clientEmailField);
        formPanel.add(Box.createVerticalStrut(10));

JPanel dateTimePanel = new JPanel(new GridLayout(1, 2, 15, 0));
        dateTimePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateTimePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel datePanel = new JPanel();
        datePanel.setLayout(new BoxLayout(datePanel, BoxLayout.Y_AXIS));
        datePanel.add(createFieldLabel("Date *"));
        dateChooser = new com.toedter.calendar.JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setDate(new Date());
        dateChooser.setMinSelectableDate(new Date());
        dateChooser.getCalendarButton().setBackground(ACCENT_BLUE);
        dateChooser.getCalendarButton().setForeground(Color.WHITE);
        dateChooser.getCalendarButton().putClientProperty("JButton.buttonType", "primary");
        dateChooser.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        dateChooser.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JTextField dateTextField = (JTextField) dateChooser.getDateEditor().getUiComponent();
        dateTextField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dateTextField.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                dateChooser.getCalendarButton().doClick();
            }
        });
        datePanel.add(dateChooser);
        dateTimePanel.add(datePanel);

        JPanel timePanel = new JPanel();
        timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.Y_AXIS));
        timePanel.add(createFieldLabel("Time *"));

        SpinnerDateModel timeModel = new SpinnerDateModel();
        timeSpinner = new JSpinner(timeModel);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);
        timeSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        timeSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        timeSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 9);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        timeSpinner.setValue(cal.getTime());
        timePanel.add(timeSpinner);
        dateTimePanel.add(timePanel);

        formPanel.add(dateTimePanel);
        formPanel.add(Box.createVerticalStrut(10));

formPanel.add(createFieldLabel("Service Type *"));
        serviceTypeCombo = new JComboBox<>();
        loadServiceComboBox();
        serviceTypeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        serviceTypeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        serviceTypeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(serviceTypeCombo);
        formPanel.add(Box.createVerticalStrut(10));

formPanel.add(createFieldLabel("Assigned Staff *"));
        assignedStaffCombo = new JComboBox<>(AppointmentService.STAFF_LIST);
        assignedStaffCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        assignedStaffCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        assignedStaffCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(assignedStaffCombo);
        formPanel.add(Box.createVerticalStrut(10));

formPanel.add(createFieldLabel("Remarks"));
        remarksArea = new JTextArea(2, 30);
        remarksArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        remarksArea.setLineWrap(true);
        remarksArea.setWrapStyleWord(true);
        remarksArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(6, 8, 6, 8)
        ));
        JScrollPane remarksScroll = new JScrollPane(remarksArea);
        remarksScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        remarksScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        remarksScroll.setBorder(null);
        formPanel.add(remarksScroll);

        add(formPanel, BorderLayout.CENTER);

JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_LIGHT));

        JButton cancelButton = createDialogButton("Cancel", null);
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);

        JButton saveButton = createDialogButton(isEditMode ? "Update" : "Save", ACCENT_BLUE);
        saveButton.addActionListener(e -> saveAppointment());
        buttonPanel.add(saveButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadServiceComboBox() {
        try {
            List<Service> dbServices = new ServiceDAO().getAllServices();
            if (dbServices.isEmpty()) {
                for (String s : AppointmentService.SERVICE_TYPES) {
                    serviceTypeCombo.addItem(s);
                }
            } else {
                for (Service s : dbServices) {
                    serviceTypeCombo.addItem(s.getName());
                }
            }
        } catch (Exception e) {
            for (String s : AppointmentService.SERVICE_TYPES) {
                serviceTypeCombo.addItem(s);
            }
        }
    }

    private void populateFields(Appointment apt) {
        clientNameField.setText(apt.getClientName());
        contactNumberField.setText(apt.getContactNumber());
        clientEmailField.setText(apt.getClientEmail() != null ? apt.getClientEmail() : "");

        if (apt.getAppointmentDate() != null) {
            dateChooser.setDate(Date.from(apt.getAppointmentDate()
                    .atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }

        if (apt.getAppointmentTime() != null) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, apt.getAppointmentTime().getHour());
            cal.set(Calendar.MINUTE, apt.getAppointmentTime().getMinute());
            cal.set(Calendar.SECOND, 0);
            timeSpinner.setValue(cal.getTime());
        }

        serviceTypeCombo.setSelectedItem(apt.getServiceType());
        assignedStaffCombo.setSelectedItem(apt.getAssignedStaff());
        remarksArea.setText(apt.getRemarks());
    }

    private void saveAppointment() {
        String clientName = clientNameField.getText().trim();
        String contactNumber = contactNumberField.getText().trim();
        String clientEmail = clientEmailField.getText().trim();
        Date selectedDate = dateChooser.getDate();
        Date selectedTime = (Date) timeSpinner.getValue();
        String serviceType = (String) serviceTypeCombo.getSelectedItem();
        String assignedStaff = (String) assignedStaffCombo.getSelectedItem();
        String remarks = remarksArea.getText().trim();

        if (clientName.isEmpty()) {
            highlightError(clientNameField);
            JOptionPane.showMessageDialog(this, "Client name is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (contactNumber.isEmpty()) {
            highlightError(contactNumberField);
            JOptionPane.showMessageDialog(this, "Contact number is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (clientEmail.isEmpty()) {
            highlightError(clientEmailField);
            JOptionPane.showMessageDialog(this, "Client email is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!ValidationUtil.isValidEmail(clientEmail)) {
            highlightError(clientEmailField);
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selectedDate == null) {
            JOptionPane.showMessageDialog(this, "Please select an appointment date.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate date = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Calendar timeCal = Calendar.getInstance();
        timeCal.setTime(selectedTime);
        LocalTime time = LocalTime.of(timeCal.get(Calendar.HOUR_OF_DAY), timeCal.get(Calendar.MINUTE));

        appointment.setClientName(clientName);
        appointment.setContactNumber(contactNumber);
        appointment.setClientEmail(clientEmail);
        appointment.setAppointmentDate(date);
        appointment.setAppointmentTime(time);
        appointment.setServiceType(serviceType);
        appointment.setAssignedStaff(assignedStaff);
        appointment.setRemarks(remarks);

        confirmed = true;
        dispose();
    }

    private void highlightError(JTextField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ERROR_BORDER, 2),
                new EmptyBorder(6, 10, 6, 10)
        ));

        Timer timer = new Timer(2000, e -> {
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                    new EmptyBorder(6, 10, 6, 10)
            ));
        });
        timer.setRepeats(false);
        timer.start();
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(0, 0, 4, 0));
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    private JButton createDialogButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        if (bgColor != null) {
            button.setBackground(bgColor);
            button.setForeground(Color.WHITE);
            button.putClientProperty("JButton.buttonType", "primary");
        }
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(110, 36));
        return button;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Appointment getAppointment() {
        return appointment;
    }
}

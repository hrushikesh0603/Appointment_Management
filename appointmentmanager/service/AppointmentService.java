package com.appointmentmanager.service;

import com.appointmentmanager.dao.AppointmentDAO;
import com.appointmentmanager.dao.ClientDAO;
import com.appointmentmanager.dao.ServiceDAO;
import com.appointmentmanager.model.Appointment;
import com.appointmentmanager.model.Client;
import com.appointmentmanager.model.Service;
import com.appointmentmanager.util.ValidationUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentService {

    private final AppointmentDAO appointmentDAO;
    private final EmailService emailService;
    private final ClientDAO clientDAO;
    private final ServiceDAO serviceDAO;

public static final String[] STAFF_LIST = {
            "Dr. Rajesh Sharma", "Dr. Sunita Verma", "Dr. Priya Iyer", "Dr. Chethan Badiger", "Dr. Sadananda Reddy"
    };

public static final String[] SERVICE_TYPES = {
            "Consultation", "Follow-up", "Treatment", "Dental Cleaning", "Routine Checkup", "X-Ray", "Tooth Extraction"
    };

public static final int WORK_START_HOUR = 9;
    public static final int WORK_END_HOUR = 18;
    public static final int SLOT_DURATION_MINUTES = 60;

    public AppointmentService() {
        this.appointmentDAO = new AppointmentDAO();
        this.emailService = EmailService.getInstance();
        this.clientDAO = new ClientDAO();
        this.serviceDAO = new ServiceDAO();
    }

    public int addAppointment(Appointment appointment) throws SQLException {
        
        validateAppointment(appointment);

resolveClientAndService(appointment);

if (!appointmentDAO.isSlotAvailable(
                appointment.getAssignedStaff(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                -1)) {
            throw new IllegalStateException(
                    "Scheduling conflict: " + appointment.getAssignedStaff() +
                    " already has an appointment at " + appointment.getAppointmentTime() +
                    " on " + appointment.getAppointmentDate());
        }

        int id = appointmentDAO.insertAppointment(appointment);

if (id > 0) {
            appointment.setId(id);
            emailService.sendConfirmationEmail(appointment);
        }

        return id;
    }

    public boolean updateAppointment(Appointment appointment) throws SQLException {
        
        validateAppointment(appointment);

resolveClientAndService(appointment);

if (!appointmentDAO.isSlotAvailable(
                appointment.getAssignedStaff(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                appointment.getId())) {
            throw new IllegalStateException(
                    "Scheduling conflict: " + appointment.getAssignedStaff() +
                    " already has an appointment at " + appointment.getAppointmentTime() +
                    " on " + appointment.getAppointmentDate());
        }

        boolean success = appointmentDAO.updateAppointment(appointment);

        return success;
    }

    public boolean cancelAppointment(int id) throws SQLException {
        
        Appointment appt = appointmentDAO.getAppointmentById(id);
        boolean success = appointmentDAO.deleteAppointment(id);
        
        if (success && appt != null) {
            emailService.sendCancellationEmail(appt);
        }
        
        return success;
    }

    public List<Appointment> getAllAppointments() throws SQLException {
        autoCompletePastAppointmentsSync();
        return appointmentDAO.getAllAppointments();
    }

    public Appointment getAppointmentById(int id) throws SQLException {
        return appointmentDAO.getAppointmentById(id);
    }

    public List<Appointment> searchAppointments(String field, String value) throws SQLException {
        autoCompletePastAppointmentsSync();
        if (value == null || value.trim().isEmpty()) {
            return getAllAppointments();
        }
        return appointmentDAO.searchAppointments(field, value.trim());
    }

    public List<Appointment> getAppointmentsForDateRange(LocalDate start, LocalDate end) throws SQLException {
        autoCompletePastAppointmentsSync();
        return appointmentDAO.getAppointmentsForDateRange(start, end);
    }

    public List<LocalTime> getAvailableSlots(String staff, LocalDate date) throws SQLException {
        List<LocalTime> allSlots = generateAllSlots();
        List<Appointment> bookedAppointments = appointmentDAO.getAppointmentsByStaffAndDate(staff, date);

List<LocalTime> bookedTimes = new ArrayList<>();
        for (Appointment a : bookedAppointments) {
            bookedTimes.add(a.getAppointmentTime());
        }

        List<LocalTime> availableSlots = new ArrayList<>();
        for (LocalTime slot : allSlots) {
            if (!bookedTimes.contains(slot)) {
                availableSlots.add(slot);
            }
        }

        return availableSlots;
    }

    public List<Appointment> getBookedSlots(String staff, LocalDate date) throws SQLException {
        autoCompletePastAppointmentsSync();
        return appointmentDAO.getAppointmentsByStaffAndDate(staff, date);
    }

    public void autoCompletePastAppointmentsSync() {
        autoCompletePastAppointmentsSyncCount();
    }

    public int autoCompletePastAppointmentsSyncCount() {
        int count = 0;
        try {
            List<Appointment> pastAppts = appointmentDAO.getPastAppointments();
            for (Appointment appt : pastAppts) {
                if (!appt.isFeedbackSent()) {
                    appt.setFeedbackSent(true);
                    boolean success = appointmentDAO.updateAppointment(appt);
                    if (success) {
                        count++;
                        
                        emailService.sendFeedbackRequestEmail(appt);
                    }
                }
            }
            if (count > 0) {
                System.out.println("Dispatched " + count + " past feedback request emails.");
            }
        } catch (Exception e) {
            System.err.println("Error processing past appointments feedback emails: " + e.getMessage());
            e.printStackTrace();
        }
        return count;
    }

    public List<LocalTime> generateAllSlots() {
        List<LocalTime> slots = new ArrayList<>();
        for (int hour = WORK_START_HOUR; hour < WORK_END_HOUR; hour++) {
            slots.add(LocalTime.of(hour, 0));
        }
        return slots;
    }

private void resolveClientAndService(Appointment appointment) throws SQLException {
        
        String phone = appointment.getContactNumber();
        if (phone != null && !phone.trim().isEmpty()) {
            Client client = clientDAO.getClientByPhone(phone);
            if (client == null) {
                
                client = new Client(
                    appointment.getClientName(),
                    appointment.getContactNumber(),
                    appointment.getClientEmail()
                );
                int clientId = clientDAO.insertClient(client);
                appointment.setClientId(clientId);
            } else {
                
                boolean changed = false;
                if (!client.getName().equalsIgnoreCase(appointment.getClientName())) {
                    client.setName(appointment.getClientName());
                    changed = true;
                }
                if (appointment.getClientEmail() != null && !appointment.getClientEmail().equalsIgnoreCase(client.getEmail())) {
                    client.setEmail(appointment.getClientEmail());
                    changed = true;
                }
                if (changed) {
                    clientDAO.updateClient(client);
                }
                appointment.setClientId(client.getId());
            }
        }

String serviceType = appointment.getServiceType();
        if (serviceType != null && !serviceType.trim().isEmpty()) {
            Service service = serviceDAO.getServiceByName(serviceType);
            if (service != null) {
                appointment.setServiceId(service.getId());
            }
        }
    }

    private void validateAppointment(Appointment appointment) {
        List<String> errors = new ArrayList<>();

        if (!ValidationUtil.isNotEmpty(appointment.getClientName())) {
            errors.add("Client name is required.");
        }

        if (!ValidationUtil.isNotEmpty(appointment.getContactNumber())) {
            errors.add("Contact number is required.");
        } else if (!ValidationUtil.isValidPhoneNumber(appointment.getContactNumber())) {
            errors.add("Invalid contact number format. Use 10+ digits.");
        }

        if (!ValidationUtil.isNotEmpty(appointment.getClientEmail())) {
            errors.add("Client email is required.");
        } else if (!ValidationUtil.isValidEmail(appointment.getClientEmail())) {
            errors.add("Invalid email address format.");
        }

        if (appointment.getAppointmentDate() == null) {
            errors.add("Appointment date is required.");
        } else {
            if (appointment.getId() <= 0) {
                
                if (!ValidationUtil.isValidDate(appointment.getAppointmentDate())) {
                    errors.add("Appointment date cannot be in the past.");
                }
            } else {
                
                try {
                    Appointment oldAppt = appointmentDAO.getAppointmentById(appointment.getId());
                    if (oldAppt != null && !oldAppt.getAppointmentDate().equals(appointment.getAppointmentDate())) {
                        if (!ValidationUtil.isValidDate(appointment.getAppointmentDate())) {
                            errors.add("Cannot change appointment date to a date in the past.");
                        }
                    }
                } catch (SQLException e) {
                    
                }
            }
        }

        if (appointment.getAppointmentTime() == null) {
            errors.add("Appointment time is required.");
        }

        if (!ValidationUtil.isNotEmpty(appointment.getServiceType())) {
            errors.add("Service type is required.");
        }

        if (!ValidationUtil.isNotEmpty(appointment.getAssignedStaff())) {
            errors.add("Assigned staff is required.");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errors));
        }
    }
}

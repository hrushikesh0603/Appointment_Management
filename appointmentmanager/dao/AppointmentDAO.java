package com.appointmentmanager.dao;

import com.appointmentmanager.model.Appointment;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AppointmentDAO {

    private final DatabaseConnection dbConnection;

    public AppointmentDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    public int insertAppointment(Appointment appointment) throws SQLException {
        String sql = "INSERT INTO appointments (client_name, contact_number, client_email, appointment_date, " +
                     "appointment_time, service_type, assigned_staff, remarks, service_id, client_id, reminder_sent, feedback_sent) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, appointment.getClientName());
            stmt.setString(2, appointment.getContactNumber());
            stmt.setString(3, appointment.getClientEmail());
            stmt.setDate(4, Date.valueOf(appointment.getAppointmentDate()));
            stmt.setTime(5, Time.valueOf(appointment.getAppointmentTime()));
            stmt.setString(6, appointment.getServiceType());
            stmt.setString(7, appointment.getAssignedStaff());
            stmt.setString(8, appointment.getRemarks());
            if (appointment.getServiceId() != null) {
                stmt.setInt(9, appointment.getServiceId());
            } else {
                stmt.setNull(9, Types.INTEGER);
            }
            if (appointment.getClientId() != null) {
                stmt.setInt(10, appointment.getClientId());
            } else {
                stmt.setNull(10, Types.INTEGER);
            }
            stmt.setBoolean(11, appointment.isReminderSent());
            stmt.setBoolean(12, appointment.isFeedbackSent());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        }
        return -1;
    }

    public List<Appointment> getAllAppointments() throws SQLException {
        String sql = "SELECT * FROM appointments ORDER BY id";
        List<Appointment> appointments = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                appointments.add(mapResultSetToAppointment(rs));
            }
        }
        return appointments;
    }

    public Appointment getAppointmentById(int id) throws SQLException {
        String sql = "SELECT * FROM appointments WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAppointment(rs);
                }
            }
        }
        return null;
    }

    public boolean updateAppointment(Appointment appointment) throws SQLException {
        String sql = "UPDATE appointments SET client_name = ?, contact_number = ?, client_email = ?, appointment_date = ?, " +
                     "appointment_time = ?, service_type = ?, assigned_staff = ?, remarks = ?, " +
                     "service_id = ?, client_id = ?, reminder_sent = ?, feedback_sent = ? WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, appointment.getClientName());
            stmt.setString(2, appointment.getContactNumber());
            stmt.setString(3, appointment.getClientEmail());
            stmt.setDate(4, Date.valueOf(appointment.getAppointmentDate()));
            stmt.setTime(5, Time.valueOf(appointment.getAppointmentTime()));
            stmt.setString(6, appointment.getServiceType());
            stmt.setString(7, appointment.getAssignedStaff());
            stmt.setString(8, appointment.getRemarks());
            if (appointment.getServiceId() != null) {
                stmt.setInt(9, appointment.getServiceId());
            } else {
                stmt.setNull(9, Types.INTEGER);
            }
            if (appointment.getClientId() != null) {
                stmt.setInt(10, appointment.getClientId());
            } else {
                stmt.setNull(10, Types.INTEGER);
            }
            stmt.setBoolean(11, appointment.isReminderSent());
            stmt.setBoolean(12, appointment.isFeedbackSent());
            stmt.setInt(13, appointment.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteAppointment(int id) throws SQLException {
        String deleteSql = "DELETE FROM appointments WHERE id = ?";

        try (Connection conn = dbConnection.getConnection()) {
            boolean deleted;
            try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
                stmt.setInt(1, id);
                deleted = stmt.executeUpdate() > 0;
            }

            if (deleted) {
                rearrangeIds(conn);
            }

            return deleted;
        }
    }

    private void rearrangeIds(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SET @count = 0")) {
            stmt.execute();
        }

        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE appointments SET id = (@count := @count + 1) ORDER BY id")) {
            stmt.executeUpdate();
        }

        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT COALESCE(MAX(id), 0) + 1 AS next_id FROM appointments")) {
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int nextId = rs.getInt("next_id");
                    try (PreparedStatement alterStmt = conn.prepareStatement(
                            "ALTER TABLE appointments AUTO_INCREMENT = " + nextId)) {
                        alterStmt.execute();
                    }
                }
            }
        }
    }

    public List<Appointment> searchAppointments(String field, String value) throws SQLException {
        String column;
        switch (field.toLowerCase()) {
            case "client name":
            case "client_name":
                column = "client_name";
                break;
            case "contact number":
            case "contact_number":
                column = "contact_number";
                break;
            case "client email":
            case "client_email":
                column = "client_email";
                break;
            case "appointment date":
            case "appointment_date":
                column = "appointment_date";
                break;
            case "service type":
            case "service_type":
                column = "service_type";
                break;
            case "assigned staff":
            case "assigned_staff":
                column = "assigned_staff";
                break;
            default:
                throw new IllegalArgumentException("Invalid search field: " + field);
        }

        String sql;
        if (column.equals("appointment_date")) {
            sql = "SELECT * FROM appointments WHERE " + column + " = ? ORDER BY id";
        } else {
            sql = "SELECT * FROM appointments WHERE " + column + " LIKE ? ORDER BY id";
        }

        List<Appointment> appointments = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (column.equals("appointment_date")) {
                stmt.setDate(1, Date.valueOf(value));
            } else {
                stmt.setString(1, "%" + value + "%");
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapResultSetToAppointment(rs));
                }
            }
        }
        return appointments;
    }

    public List<Appointment> getAppointmentsByStaffAndDate(String staff, LocalDate date) throws SQLException {
        String sql = "SELECT * FROM appointments WHERE assigned_staff = ? AND appointment_date = ? " +
                     "ORDER BY appointment_time";
        List<Appointment> appointments = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, staff);
            stmt.setDate(2, Date.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapResultSetToAppointment(rs));
                }
            }
        }
        return appointments;
    }

    public List<Appointment> getAppointmentsForDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT * FROM appointments WHERE appointment_date BETWEEN ? AND ? " +
                     "ORDER BY appointment_date, appointment_time";
        List<Appointment> appointments = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapResultSetToAppointment(rs));
                }
            }
        }
        return appointments;
    }

    public List<Appointment> getPastAppointments() throws SQLException {
        String sql = "SELECT * FROM appointments " +
                     "WHERE (appointment_date < CURDATE() " +
                     "  OR (appointment_date = CURDATE() AND appointment_time < CURTIME())) " +
                     "ORDER BY id";
        List<Appointment> appointments = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                appointments.add(mapResultSetToAppointment(rs));
            }
        }
        return appointments;
    }

    public boolean isSlotAvailable(String staff, LocalDate date, LocalTime time, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM appointments WHERE assigned_staff = ? AND appointment_date = ? " +
                     "AND appointment_time = ? AND id != ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, staff);
            stmt.setDate(2, Date.valueOf(date));
            stmt.setTime(3, Time.valueOf(time));
            stmt.setInt(4, excludeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        }
        return true;
    }

public List<Appointment> getAppointmentsPendingReminder() throws SQLException {
        
        String sql = "SELECT * FROM appointments WHERE reminder_sent = FALSE " +
                     "AND client_email IS NOT NULL AND client_email != '' " +
                     "AND appointment_date = DATE_ADD(CURDATE(), INTERVAL 1 DAY)";
        
        List<Appointment> appointments = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                appointments.add(mapResultSetToAppointment(rs));
            }
        }
        return appointments;
    }

    public boolean markReminderSent(int id) throws SQLException {
        String sql = "UPDATE appointments SET reminder_sent = TRUE WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

public Map<String, Object> getDashboardStats() throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        
        String sqlTotal = "SELECT COUNT(*) FROM appointments";
        String sqlNext = "SELECT COUNT(*) FROM appointments " +
                         "WHERE (appointment_date > CURDATE() " +
                         "  OR (appointment_date = CURDATE() AND appointment_time >= CURTIME()))";
        String sqlRevenue = "SELECT COALESCE(SUM(s.price), 0.0) FROM appointments a " +
                             "JOIN services s ON a.service_id = s.id " +
                             "WHERE (a.appointment_date < CURDATE() " +
                             "  OR (a.appointment_date = CURDATE() AND a.appointment_time < CURTIME()))";

        try (Connection conn = dbConnection.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sqlTotal)) {
                    stats.put("total_bookings", rs.next() ? rs.getInt(1) : 0);
                }
                try (ResultSet rs = stmt.executeQuery(sqlNext)) {
                    stats.put("next_bookings", rs.next() ? rs.getInt(1) : 0);
                }
                try (ResultSet rs = stmt.executeQuery(sqlRevenue)) {
                    stats.put("total_revenue", rs.next() ? rs.getDouble(1) : 0.0);
                }
            }
        }
        return stats;
    }

    public Map<String, Integer> getPopularServicesData() throws SQLException {
        String sql = "SELECT s.name as service_type, COUNT(a.id) as cnt " +
                     "FROM services s " +
                     "LEFT JOIN appointments a ON (s.id = a.service_id OR s.name = a.service_type) " +
                     "GROUP BY s.id, s.name " +
                     "ORDER BY cnt DESC, s.name ASC";
        
        Map<String, Integer> data = new LinkedHashMap<>(); 
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                data.put(rs.getString("service_type"), rs.getInt("cnt"));
            }
        }
        return data;
    }

    public Map<String, Integer> getStaffWorkloadData() throws SQLException {
        Map<String, Integer> data = new LinkedHashMap<>();
        
        for (String staff : com.appointmentmanager.service.AppointmentService.STAFF_LIST) {
            data.put(staff, 0);
        }
        
        String sql = "SELECT assigned_staff, COUNT(*) as cnt FROM appointments " +
                     "WHERE assigned_staff IS NOT NULL AND assigned_staff != '' " +
                     "GROUP BY assigned_staff";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String staffName = rs.getString("assigned_staff");
                data.put(staffName, rs.getInt("cnt"));
            }
        }

List<Map.Entry<String, Integer>> list = new ArrayList<>(data.entrySet());
        list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        
        Map<String, Integer> sortedData = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedData.put(entry.getKey(), entry.getValue());
        }
        return sortedData;
    }

    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        int serviceIdVal = rs.getInt("service_id");
        Integer serviceId = rs.wasNull() ? null : serviceIdVal;

        int clientIdVal = rs.getInt("client_id");
        Integer clientId = rs.wasNull() ? null : clientIdVal;

        return new Appointment(
                rs.getInt("id"),
                rs.getString("client_name"),
                rs.getString("contact_number"),
                rs.getString("client_email"),
                rs.getDate("appointment_date").toLocalDate(),
                rs.getTime("appointment_time").toLocalTime(),
                rs.getString("service_type"),
                rs.getString("assigned_staff"),
                rs.getString("remarks"),
                serviceId,
                clientId,
                rs.getBoolean("reminder_sent"),
                rs.getBoolean("feedback_sent")
        );
    }
}
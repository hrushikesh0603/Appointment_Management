package com.appointmentmanager.dao;

import com.appointmentmanager.model.Appointment;
import com.appointmentmanager.model.Feedback;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDAO {
    private final DatabaseConnection dbConnection;

    public FeedbackDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    public int insertFeedback(Feedback feedback) throws SQLException {
        String sql = "INSERT INTO feedbacks (appointment_id, client_id, rating, comments) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            if (feedback.getAppointmentId() != null) {
                stmt.setInt(1, feedback.getAppointmentId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            if (feedback.getClientId() != null) {
                stmt.setInt(2, feedback.getClientId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setInt(3, feedback.getRating());
            stmt.setString(4, feedback.getComments());

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

    public List<Feedback> getAllFeedbacks() throws SQLException {
        String sql = "SELECT f.*, c.name AS client_name, a.appointment_date FROM feedbacks f " +
                     "LEFT JOIN clients c ON f.client_id = c.id " +
                     "LEFT JOIN appointments a ON f.appointment_id = a.id " +
                     "ORDER BY f.created_at DESC";
        
        List<Feedback> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                int appointmentId = rs.getInt("appointment_id");
                Integer aId = rs.wasNull() ? null : appointmentId;
                
                int clientId = rs.getInt("client_id");
                Integer cId = rs.wasNull() ? null : clientId;

                Date apptDate = rs.getDate("appointment_date");
                String dateStr = apptDate != null ? apptDate.toString() : "N/A";

                list.add(new Feedback(
                    rs.getInt("id"),
                    aId,
                    cId,
                    rs.getString("client_name") != null ? rs.getString("client_name") : "Unknown",
                    dateStr,
                    rs.getInt("rating"),
                    rs.getString("comments"),
                    rs.getTimestamp("created_at").toString()
                ));
            }
        }
        return list;
    }

    public List<Appointment> getCompletedAppointmentsWithoutFeedback() throws SQLException {
        String sql = "SELECT a.* FROM appointments a " +
                     "LEFT JOIN feedbacks f ON a.id = f.appointment_id " +
                     "WHERE (a.appointment_date < CURDATE() OR (a.appointment_date = CURDATE() AND a.appointment_time < CURTIME())) " +
                     "AND f.id IS NULL " +
                     "ORDER BY a.appointment_date DESC, a.appointment_time DESC";
        
        List<Appointment> appointments = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                int serviceId = rs.getInt("service_id");
                Integer sId = rs.wasNull() ? null : serviceId;

                int clientId = rs.getInt("client_id");
                Integer cId = rs.wasNull() ? null : clientId;

                appointments.add(new Appointment(
                    rs.getInt("id"),
                    rs.getString("client_name"),
                    rs.getString("contact_number"),
                    rs.getString("client_email"),
                    rs.getDate("appointment_date").toLocalDate(),
                    rs.getTime("appointment_time").toLocalTime(),
                    rs.getString("service_type"),
                    rs.getString("assigned_staff"),
                    rs.getString("remarks"),
                    sId,
                    cId,
                    rs.getBoolean("reminder_sent"),
                    rs.getBoolean("feedback_sent")
                ));
            }
        }
        return appointments;
    }
}

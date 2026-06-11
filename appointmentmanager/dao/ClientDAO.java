package com.appointmentmanager.dao;

import com.appointmentmanager.model.Appointment;
import com.appointmentmanager.model.Client;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {
    private final DatabaseConnection dbConnection;

    public ClientDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    public List<Client> getAllClients() throws SQLException {
        String sql = "SELECT * FROM clients ORDER BY id";
        List<Client> clients = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                clients.add(mapResultSetToClient(rs));
            }
        }
        return clients;
    }

    public Client getClientById(int id) throws SQLException {
        String sql = "SELECT * FROM clients WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToClient(rs);
                }
            }
        }
        return null;
    }

    public Client getClientByPhone(String phone) throws SQLException {
        String sql = "SELECT * FROM clients WHERE phone = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToClient(rs);
                }
            }
        }
        return null;
    }

    public int insertClient(Client client) throws SQLException {
        String sql = "INSERT INTO clients (name, phone, email) VALUES (?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, client.getName());
            stmt.setString(2, client.getPhone());
            stmt.setString(3, client.getEmail());
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

    public boolean updateClient(Client client) throws SQLException {
        String sql = "UPDATE clients SET name = ?, phone = ?, email = ? WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, client.getName());
            stmt.setString(2, client.getPhone());
            stmt.setString(3, client.getEmail());
            stmt.setInt(4, client.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteClient(int id) throws SQLException {
        String sql = "DELETE FROM clients WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Appointment> getClientAppointmentHistory(int clientId) throws SQLException {
        String sql = "SELECT * FROM appointments WHERE client_id = ? ORDER BY appointment_date DESC, appointment_time DESC";
        List<Appointment> history = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    history.add(new Appointment(
                        rs.getInt("id"),
                        rs.getString("client_name"),
                        rs.getString("contact_number"),
                        rs.getString("client_email"),
                        rs.getDate("appointment_date").toLocalDate(),
                        rs.getTime("appointment_time").toLocalTime(),
                        rs.getString("service_type"),
                        rs.getString("assigned_staff"),
                        rs.getString("remarks"),
                        rs.getObject("service_id") != null ? rs.getInt("service_id") : null,
                        rs.getObject("client_id") != null ? rs.getInt("client_id") : null,
                        rs.getBoolean("reminder_sent"),
                        rs.getBoolean("feedback_sent")
                    ));
                }
            }
        }
        return history;
    }

    private Client mapResultSetToClient(ResultSet rs) throws SQLException {
        return new Client(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("phone"),
            rs.getString("email")
        );
    }
}

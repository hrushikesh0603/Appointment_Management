package com.appointmentmanager.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static final DatabaseConnection instance = new DatabaseConnection();
    private String url;
    private String username;
    private String password;

    private DatabaseConnection() {
        loadProperties();
    }

private void loadProperties() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("app.properties")) {
            if (input == null) {
                System.err.println("Unable to find app.properties. Using default values.");
                url = "jdbc:mysql://localhost:3306/appointment_db";
                username = "root";
                password = "";
                return;
            }
            props.load(input);
            url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/appointment_db");
            username = props.getProperty("db.username", "root");
            password = props.getProperty("db.password", "");
        } catch (IOException e) {
            System.err.println("Error loading app.properties: " + e.getMessage());
            url = "jdbc:mysql://localhost:3306/appointment_db";
            username = "root";
            password = "";
        }
    }

public static DatabaseConnection getInstance() {
        return instance;
    }

public Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found: " + e.getMessage());
        }
    }

public boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}

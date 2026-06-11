package com.appointmentmanager;

import com.appointmentmanager.dao.DatabaseConnection;
import com.appointmentmanager.view.LoginFrame;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

public class App {

    public static void main(String[] args) {
        try {
            FlatLightLaf.setup();
            UIManager.put("Component.arc", 8);
            UIManager.put("Button.arc", 8);
            UIManager.put("TextComponent.arc", 6);
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("TabbedPane.showTabSeparators", true);
        } catch (Exception e) {
            System.err.println("Failed to set FlatLaf L&F: " + e.getMessage());
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {

            }
        }

        if (!DatabaseConnection.getInstance().testConnection()) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                        "Cannot connect to the database.\n\n" +
                        "Please ensure:\n" +
                        "1. MySQL is running on localhost:3306\n" +
                        "2. Database 'appointment_db' exists\n" +
                        "3. Credentials in app.properties are correct\n\n" +
                        "Run the sql/setup.sql script to initialize the database.",
                        "Database Connection Error",
                        JOptionPane.ERROR_MESSAGE);
            });
        }

SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}

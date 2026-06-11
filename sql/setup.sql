-- ============================================
-- Appointment Management System - Database Setup
-- ============================================

CREATE DATABASE IF NOT EXISTS appointment_db;
USE appointment_db;

-- Users table for authentication
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'STAFF') DEFAULT 'STAFF',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Appointments table
CREATE TABLE IF NOT EXISTS appointments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    client_name VARCHAR(255) NOT NULL,
    contact_number VARCHAR(20) NOT NULL,
    client_email VARCHAR(255),
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    service_type VARCHAR(100) NOT NULL,
    assigned_staff VARCHAR(255) NOT NULL,
    remarks VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Index for common search queries
    INDEX idx_client_name (client_name),
    INDEX idx_appointment_date (appointment_date),
    INDEX idx_assigned_staff (assigned_staff),
    INDEX idx_service_type (service_type),
    -- Unique constraint to prevent double-booking same staff at same date+time
    UNIQUE KEY uk_staff_datetime (assigned_staff, appointment_date, appointment_time)
);

-- Default admin user (password: admin123)
INSERT INTO users (username, password_hash, role) VALUES
('admin', SHA2('admin123', 256), 'ADMIN')
ON DUPLICATE KEY UPDATE username = username;

-- Sample data for testing
INSERT INTO appointments (client_name, contact_number, client_email, appointment_date, appointment_time, service_type, assigned_staff, remarks) VALUES
('John Doe', '9876543210', 'john.doe@email.com', CURDATE(), '10:00:00', 'Consultation', 'Dr. Rajesh Sharma', 'First visit'),
('Jane Smith', '9876543211', 'jane.smith@email.com', CURDATE(), '11:00:00', 'Follow-up', 'Dr. Amit Patel', 'Monthly checkup'),
('Robert Brown', '9876543212', 'robert.brown@email.com', DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00', 'Treatment', 'Dr. Rajesh Sharma', 'Dental cleaning'),
('Emily Davis', '9876543213', 'emily.davis@email.com', DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00:00', 'Consultation', 'Dr. Sunita Verma', 'New patient'),
('Michael Wilson', '9876543214', 'michael.wilson@email.com', DATE_ADD(CURDATE(), INTERVAL 2 DAY), '10:30:00', 'Follow-up', 'Dr. Amit Patel', 'Lab results review');

-- ============================================
-- Migration for existing databases:
-- Run this if you already have the appointments table:
--
-- ALTER TABLE appointments ADD COLUMN client_email VARCHAR(255) AFTER contact_number;
-- ============================================

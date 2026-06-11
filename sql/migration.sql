-- ============================================
-- Schema Migration: Advanced Features
-- ============================================

USE appointment_db;

-- 1. Create Services catalog table
CREATE TABLE IF NOT EXISTS services (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    duration_minutes INT NOT NULL DEFAULT 60,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Create Clients directory table
CREATE TABLE IF NOT EXISTS clients (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Alter Appointments table columns individually (to handle errors gracefully)
ALTER TABLE appointments ADD COLUMN status ENUM('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING' AFTER remarks;
ALTER TABLE appointments ADD COLUMN service_id INT NULL AFTER status;
ALTER TABLE appointments ADD COLUMN client_id INT NULL AFTER service_id;
ALTER TABLE appointments ADD COLUMN reminder_sent BOOLEAN DEFAULT FALSE AFTER client_id;

-- 4. Add constraints
ALTER TABLE appointments ADD CONSTRAINT fk_appt_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE SET NULL;
ALTER TABLE appointments ADD CONSTRAINT fk_appt_client FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE SET NULL;

-- 5. Migrate existing data from appointments table
-- Insert unique clients from historical appointments
INSERT IGNORE INTO clients (name, phone, email) 
SELECT DISTINCT client_name, contact_number, client_email 
FROM appointments 
WHERE contact_number IS NOT NULL AND contact_number != '';

-- Map appointments back to client_id
UPDATE appointments a 
JOIN clients c ON a.contact_number = c.phone 
SET a.client_id = c.id;

-- Insert unique services from historical appointments
INSERT IGNORE INTO services (name, price, duration_minutes) 
SELECT DISTINCT service_type, 500.00, 60 
FROM appointments 
WHERE service_type IS NOT NULL AND service_type != '';

-- Insert predefined services with INR prices and additional service types
INSERT INTO services (name, description, price, duration_minutes) VALUES
('Consultation', 'General consultation with specialist doctor', 500.00, 30),
('Follow-up', 'Follow-up visit for ongoing treatment review', 300.00, 20),
('Treatment', 'Standard medical/dental treatment procedures', 1200.00, 60),
('Dental Cleaning', 'Professional scaling and teeth cleaning', 1500.00, 30),
('Routine Checkup', 'Comprehensive routine health checkup', 600.00, 45),
('X-Ray', 'Dental/Medical diagnostic radiography', 1000.00, 15),
('Tooth Extraction', 'Safe removal of damaged or impacted tooth', 2000.00, 45)
ON DUPLICATE KEY UPDATE 
price = VALUES(price), 
duration_minutes = VALUES(duration_minutes),
description = VALUES(description);


-- Map appointments back to service_id
UPDATE appointments a 
JOIN services s ON a.service_type = s.name 
SET a.service_id = s.id;

-- Set all existing appointments status to CONFIRMED
UPDATE appointments SET status = 'CONFIRMED';

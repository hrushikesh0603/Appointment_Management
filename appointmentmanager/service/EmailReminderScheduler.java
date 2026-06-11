package com.appointmentmanager.service;

import com.appointmentmanager.dao.AppointmentDAO;
import com.appointmentmanager.model.Appointment;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EmailReminderScheduler {

    private static EmailReminderScheduler instance;
    private ScheduledExecutorService scheduler;
    private final AppointmentDAO appointmentDAO;
    private final EmailService emailService;
    private boolean running = false;

    private EmailReminderScheduler() {
        this.appointmentDAO = new AppointmentDAO();
        this.emailService = EmailService.getInstance();
    }

    public static synchronized EmailReminderScheduler getInstance() {
        if (instance == null) {
            instance = new EmailReminderScheduler();
        }
        return instance;
    }

public synchronized void start() {
        if (running) {
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "EmailReminderScheduler-Thread");
            thread.setDaemon(true); 
            return thread;
        });

scheduler.scheduleAtFixedRate(this::checkAndSendReminders, 10, 1800, TimeUnit.SECONDS);
        running = true;
        System.out.println("Email reminder scheduler started.");
    }

public synchronized void stop() {
        if (!running || scheduler == null) {
            return;
        }
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        running = false;
        System.out.println("Email reminder scheduler stopped.");
    }

private void checkAndSendReminders() {
        try {
            System.out.println("Running background check for pending email reminders...");
            List<Appointment> pendingReminders = appointmentDAO.getAppointmentsPendingReminder();
            
            if (pendingReminders.isEmpty()) {
                System.out.println("No pending email reminders found for tomorrow.");
                return;
            }

            System.out.println("Found " + pendingReminders.size() + " appointments scheduled for tomorrow requiring reminders.");
            for (Appointment appt : pendingReminders) {
                
                emailService.sendReminderEmail(appt);

appointmentDAO.markReminderSent(appt.getId());
            }
        } catch (SQLException e) {
            System.err.println("Error while running background reminder check: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

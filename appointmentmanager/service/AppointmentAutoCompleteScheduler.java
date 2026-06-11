package com.appointmentmanager.service;

import com.appointmentmanager.view.MainFrame;

import javax.swing.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AppointmentAutoCompleteScheduler {

    private static AppointmentAutoCompleteScheduler instance;
    private ScheduledExecutorService scheduler;
    private final AppointmentService appointmentService;
    private MainFrame mainFrame;
    private boolean running = false;

    private AppointmentAutoCompleteScheduler() {
        this.appointmentService = new AppointmentService();
    }

    public static synchronized AppointmentAutoCompleteScheduler getInstance() {
        if (instance == null) {
            instance = new AppointmentAutoCompleteScheduler();
        }
        return instance;
    }

    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public synchronized void start() {
        if (running) {
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "AppointmentAutoCompleteScheduler-Thread");
            thread.setDaemon(true); 
            return thread;
        });

scheduler.scheduleAtFixedRate(this::checkAndAutoComplete, 5, 30, TimeUnit.SECONDS);
        running = true;
        System.out.println("Appointment auto-complete scheduler started.");
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
        System.out.println("Appointment auto-complete scheduler stopped.");
    }

    private void checkAndAutoComplete() {
        try {
            int completedCount = appointmentService.autoCompletePastAppointmentsSyncCount();
            if (completedCount > 0 && mainFrame != null) {
                
                SwingUtilities.invokeLater(() -> {
                    mainFrame.getController().refreshTable();
                    if (mainFrame.getStaffAvailabilityPanel() != null) {
                        mainFrame.getStaffAvailabilityPanel().refreshAvailability();
                    }
                    if (mainFrame.getCalendarPanel() != null) {
                        mainFrame.getCalendarPanel().refreshCalendar();
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Error in background auto-complete scheduler check: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

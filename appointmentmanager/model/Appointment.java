package com.appointmentmanager.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Appointment {

    private int id;
    private String clientName;
    private String contactNumber;
    private String clientEmail;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String serviceType;
    private String assignedStaff;
    private String remarks;

private Integer serviceId;
    private Integer clientId;
    private boolean reminderSent = false;
    private boolean feedbackSent = false;

public Appointment() {
    }

public Appointment(String clientName, String contactNumber, String clientEmail, LocalDate appointmentDate,
                       LocalTime appointmentTime, String serviceType, String assignedStaff, String remarks) {
        this.clientName = clientName;
        this.contactNumber = contactNumber;
        this.clientEmail = clientEmail;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.serviceType = serviceType;
        this.assignedStaff = assignedStaff;
        this.remarks = remarks;
    }

public Appointment(int id, String clientName, String contactNumber, String clientEmail, LocalDate appointmentDate,
                       LocalTime appointmentTime, String serviceType, String assignedStaff, String remarks) {
        this.id = id;
        this.clientName = clientName;
        this.contactNumber = contactNumber;
        this.clientEmail = clientEmail;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.serviceType = serviceType;
        this.assignedStaff = assignedStaff;
        this.remarks = remarks;
    }

public Appointment(int id, String clientName, String contactNumber, String clientEmail, LocalDate appointmentDate,
                       LocalTime appointmentTime, String serviceType, String assignedStaff, String remarks,
                       Integer serviceId, Integer clientId, boolean reminderSent, boolean feedbackSent) {
        this.id = id;
        this.clientName = clientName;
        this.contactNumber = contactNumber;
        this.clientEmail = clientEmail;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.serviceType = serviceType;
        this.assignedStaff = assignedStaff;
        this.remarks = remarks;
        this.serviceId = serviceId;
        this.clientId = clientId;
        this.reminderSent = reminderSent;
        this.feedbackSent = feedbackSent;
    }

public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getAssignedStaff() {
        return assignedStaff;
    }

    public void setAssignedStaff(String assignedStaff) {
        this.assignedStaff = assignedStaff;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public boolean isReminderSent() {
        return reminderSent;
    }

    public void setReminderSent(boolean reminderSent) {
        this.reminderSent = reminderSent;
    }

    public boolean isFeedbackSent() {
        return feedbackSent;
    }

    public void setFeedbackSent(boolean feedbackSent) {
        this.feedbackSent = feedbackSent;
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", clientName='" + clientName + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", clientEmail='" + clientEmail + '\'' +
                ", appointmentDate=" + appointmentDate +
                ", appointmentTime=" + appointmentTime +
                ", serviceType='" + serviceType + '\'' +
                ", assignedStaff='" + assignedStaff + '\'' +
                ", remarks='" + remarks + '\'' +
                ", serviceId=" + serviceId +
                ", clientId=" + clientId +
                ", reminderSent=" + reminderSent +
                ", feedbackSent=" + feedbackSent +
                '}';
    }
}

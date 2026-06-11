package com.appointmentmanager.model;

public class Feedback {
    private int id;
    private Integer appointmentId;
    private Integer clientId;
    private String clientName;
    private String appointmentDate;
    private int rating;
    private String comments;
    private String createdAt;

public Feedback() {}

    public Feedback(Integer appointmentId, Integer clientId, int rating, String comments) {
        this.appointmentId = appointmentId;
        this.clientId = clientId;
        this.rating = rating;
        this.comments = comments;
    }

    public Feedback(int id, Integer appointmentId, Integer clientId, String clientName, String appointmentDate, int rating, String comments, String createdAt) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.clientId = clientId;
        this.clientName = clientName;
        this.appointmentDate = appointmentDate;
        this.rating = rating;
        this.comments = comments;
        this.createdAt = createdAt;
    }

public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}

package com.appointmentmanager.service;

import com.appointmentmanager.model.Appointment;
import com.appointmentmanager.util.DateTimeUtil;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.Properties;

public class EmailService {

    private final String smtpHost;
    private final String smtpPort;
    private final boolean smtpAuth;
    private final boolean smtpStartTls;
    private final String senderEmail;
    private final String senderPassword;
    private final boolean configured;

    private static EmailService instance;

    private EmailService() {
        Properties appProps = new Properties();
        boolean loaded = false;
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("app.properties")) {
            if (input != null) {
                appProps.load(input);
                loaded = true;
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load email configuration: " + e.getMessage());
        }

        this.smtpHost = appProps.getProperty("mail.smtp.host", "smtp.gmail.com");
        this.smtpPort = appProps.getProperty("mail.smtp.port", "587");
        this.smtpAuth = Boolean.parseBoolean(appProps.getProperty("mail.smtp.auth", "true"));
        this.smtpStartTls = Boolean.parseBoolean(appProps.getProperty("mail.smtp.starttls", "true"));
        this.senderEmail = appProps.getProperty("mail.sender.email", "");
        this.senderPassword = appProps.getProperty("mail.sender.password", "");

        this.configured = loaded
                && !senderEmail.isEmpty()
                && !senderEmail.equals("YOUR_EMAIL@gmail.com")
                && !senderPassword.isEmpty()
                && !senderPassword.equals("YOUR_APP_PASSWORD");

        if (!configured) {
            System.out.println("Email service is not configured. Update mail.sender.email and mail.sender.password in app.properties.");
        }
    }

    public static synchronized EmailService getInstance() {
        if (instance == null) {
            instance = new EmailService();
        }
        return instance;
    }

    public boolean isConfigured() {
        return configured;
    }

    public void sendConfirmationEmail(Appointment appointment) {
        sendAsync(appointment, "CONFIRMATION");
    }

    public void sendCancellationEmail(Appointment appointment) {
        sendAsync(appointment, "CANCELLATION");
    }

    public void sendReminderEmail(Appointment appointment) {
        sendAsync(appointment, "REMINDER");
    }

    public void sendFeedbackRequestEmail(Appointment appointment) {
        sendAsync(appointment, "FEEDBACK_REQUEST");
    }

    private void sendAsync(Appointment appointment, String type) {
        if (!configured) {
            System.out.println("Email not sent — email service is not configured.");
            return;
        }

        if (appointment.getClientEmail() == null || appointment.getClientEmail().trim().isEmpty()) {
            System.out.println("Email not sent — no client email address provided.");
            return;
        }

        new Thread(() -> {
            try {
                sendEmail(appointment, type);
                System.out.println(type + " email sent to: " + appointment.getClientEmail());
            } catch (Exception e) {
                System.err.println("Failed to send " + type + " email to " + appointment.getClientEmail() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }, "EmailSender-Thread-" + type).start();
    }

    private void sendEmail(Appointment appointment, String type) throws MessagingException {
        Properties mailProps = new Properties();
        mailProps.put("mail.smtp.host", smtpHost);
        mailProps.put("mail.smtp.port", smtpPort);
        mailProps.put("mail.smtp.auth", String.valueOf(smtpAuth));
        mailProps.put("mail.smtp.starttls.enable", String.valueOf(smtpStartTls));
        mailProps.put("mail.smtp.ssl.protocols", "TLSv1.2");
        mailProps.put("mail.smtp.ssl.trust", smtpHost);

        final String cleanEmail = senderEmail.trim();
        final String cleanPassword = senderPassword.replaceAll("\\s+", "");

        Session session = Session.getInstance(mailProps);

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(cleanEmail));
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(appointment.getClientEmail().trim()));
        
        String subject;
        if ("CANCELLATION".equals(type)) {
            subject = "Appointment Cancelled - " + appointment.getServiceType();
        } else if ("REMINDER".equals(type)) {
            subject = "Appointment Reminder - " + appointment.getServiceType();
        } else if ("FEEDBACK_REQUEST".equals(type)) {
            subject = "How was your visit? Share your feedback - " + appointment.getServiceType();
        } else {
            subject = "Appointment Booked with " + appointment.getAssignedStaff();
        }
        
        message.setSubject(subject);
        message.setContent(buildEmailBody(appointment, type), "text/html; charset=utf-8");

        try (Transport transport = session.getTransport("smtp")) {
            transport.connect(smtpHost, Integer.parseInt(smtpPort), cleanEmail, cleanPassword);
            transport.sendMessage(message, message.getAllRecipients());
        }
    }

    private String buildEmailBody(Appointment appointment, String type) {
        String formattedDate = DateTimeUtil.formatDateForDisplay(appointment.getAppointmentDate());
        String formattedTime = DateTimeUtil.formatTime(appointment.getAppointmentTime());

        String headerTitle;
        String headerColor1;
        String headerColor2;
        String introText;

        if ("CANCELLATION".equals(type)) {
            headerTitle = "❌ Appointment Cancelled";
            headerColor1 = "#ef4444"; 
            headerColor2 = "#b91c1c";
            introText = "Your scheduled appointment has been **cancelled**. Below are the details of the cancelled session:";
        } else if ("REMINDER".equals(type)) {
            headerTitle = "⏰ Appointment Reminder";
            headerColor1 = "#f97316"; 
            headerColor2 = "#c2410c";
            introText = "This is a friendly reminder that you have an appointment scheduled for tomorrow. Here are the details:";
        } else if ("FEEDBACK_REQUEST".equals(type)) {
            headerTitle = "⭐ Share Your Feedback";
            headerColor1 = "#8b5cf6"; 
            headerColor2 = "#6d28d9";
            introText = "Thank you for choosing us! We hope you had a wonderful visit. We would love to hear your feedback on your recent appointment:";
        } else {
            headerTitle = "📅 Appointment Booked";
            headerColor1 = "#10b981"; 
            headerColor2 = "#059669";
            introText = "Your appointment has been successfully booked on <strong>" + formattedDate + "</strong> at <strong>" + formattedTime + "</strong> with <strong>" + appointment.getAssignedStaff() + "</strong>. Below are the details of your session:";
        }

        String actionButtonSection = "";

        String feedbackStarsSection = "";
        if ("FEEDBACK_REQUEST".equals(type)) {
            feedbackStarsSection = 
                "<tr><td align='center' style='padding:10px 40px 20px;'>" +
                "<p style='color:#374151; font-size:14px; text-align:center; margin:10px 0 0 0;'>Please reply to this email with your experience in your own words to help us improve!</p>" +
                "</td></tr>";
        }

        return "<!DOCTYPE html>" +
               "<html>" +
               "<head><meta charset='UTF-8'></head>" +
               "<body style='margin:0; padding:0; background-color:#f4f4f7; font-family:Segoe UI, Arial, sans-serif;'>" +
               "<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f4f4f7; padding:40px 0;'>" +
               "<tr><td align='center'>" +
               "<table width='600' cellpadding='0' cellspacing='0' style='background-color:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 2px 8px rgba(0,0,0,0.08);'>" +

"<tr><td style='background: linear-gradient(135deg, " + headerColor1 + ", " + headerColor2 + "); padding:30px 40px; text-align:center;'>" +
               "<h1 style='color:#ffffff; margin:0; font-size:24px; font-weight:600;'>" + headerTitle + "</h1>" +
               "</td></tr>" +

"<tr><td style='padding:30px 40px 10px;'>" +
               "<p style='color:#374151; font-size:16px; margin:0;'>Dear <strong>" + escapeHtml(appointment.getClientName()) + "</strong>,</p>" +
               "<p style='color:#6b7280; font-size:14px; line-height:1.6;'>" + introText + "</p>" +
               "</td></tr>" +

"<tr><td style='padding:10px 40px 30px;'>" +
               "<table width='100%' cellpadding='12' cellspacing='0' style='background-color:#f9fafb; border-radius:8px; border:1px solid #e5e7eb;'>" +
               detailRow("📅 Date", formattedDate) +
               detailRow("🕐 Time", formattedTime) +
               detailRow("🏥 Service", escapeHtml(appointment.getServiceType())) +
               detailRow("👨‍⚕️ Doctor", escapeHtml(appointment.getAssignedStaff())) +
               (appointment.getRemarks() != null && !appointment.getRemarks().trim().isEmpty()
                       ? detailRow("📝 Remarks", escapeHtml(appointment.getRemarks()))
                       : "") +
               "</table>" +
               "</td></tr>" +

               feedbackStarsSection +

"<tr><td style='padding:0 40px 30px;'>" +
               "<p style='color:#6b7280; font-size:13px; line-height:1.5; margin:0;'>" +
               ("CANCELLATION".equals(type)
                       ? "If you believe this was an error, or would like to schedule a new appointment, please contact us."
                       : "FEEDBACK_REQUEST".equals(type)
                       ? "Your feedback helps us improve our services for you and other clients. We look forward to seeing you again!"
                       : "If you need to reschedule or cancel, please contact us in advance.<br>Thank you for choosing our services!") +
               "</p></td></tr>" +

"<tr><td style='background-color:#f9fafb; padding:20px 40px; text-align:center; border-top:1px solid #e5e7eb;'>" +
               "<p style='color:#9ca3af; font-size:12px; margin:0;'>Appointment Management System</p>" +
               "</td></tr>" +

               "</table></td></tr></table></body></html>";
    }

    private String detailRow(String label, String value) {
        return "<tr>" +
               "<td style='color:#6b7280; font-size:14px; font-weight:500; width:40%; border-bottom:1px solid #e5e7eb;'>" + label + "</td>" +
               "<td style='color:#111827; font-size:14px; font-weight:600; border-bottom:1px solid #e5e7eb;'>" + value + "</td>" +
               "</tr>";
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}

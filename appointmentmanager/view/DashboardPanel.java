package com.appointmentmanager.view;

import com.appointmentmanager.dao.AppointmentDAO;
import com.appointmentmanager.dao.ServiceDAO;
import com.appointmentmanager.model.Appointment;
import com.appointmentmanager.model.Service;
import com.appointmentmanager.util.DateTimeUtil;

import java.time.LocalDate;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DashboardPanel extends JPanel {

    private final AppointmentDAO appointmentDAO;
    private final ServiceDAO serviceDAO;
    private JLabel totalBookingsVal;
    private JLabel nextVal;
    private JLabel totalServicesVal;
    private ChartPanel popularServicesChart;
    private ChartPanel staffWorkloadChart;
    private JPanel upcomingListPanel;
    private JPanel servicesListPanel;

    public DashboardPanel() {
        this.appointmentDAO = new AppointmentDAO();
        this.serviceDAO = new ServiceDAO();
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        initComponents();
        refreshData();
    }

    private void initComponents() {
        
        JPanel metricsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        metricsPanel.setOpaque(false);
        
        metricsPanel.add(createMetricCard("Total Bookings", Color.BLUE, totalBookingsVal = new JLabel("0")));
        metricsPanel.add(createMetricCard("Next Appointments", new Color(16, 185, 129), nextVal = new JLabel("0")));
        metricsPanel.add(createMetricCard("Total Services Offered", new Color(139, 92, 246), totalServicesVal = new JLabel("0")));
        
        add(metricsPanel, BorderLayout.NORTH);

JPanel centerPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        centerPanel.setOpaque(false);

JPanel chartsContainer = new JPanel(new GridLayout(2, 1, 0, 15));
        chartsContainer.setOpaque(false);
        
        popularServicesChart = new ChartPanel("Popular Services", new Color(59, 130, 246));
        staffWorkloadChart = new ChartPanel("Staff Workload", new Color(236, 72, 153));
        
        chartsContainer.add(popularServicesChart);
        chartsContainer.add(staffWorkloadChart);
        
        centerPanel.add(chartsContainer);

JPanel upcomingContainer = new JPanel(new BorderLayout(10, 10));
        upcomingContainer.putClientProperty("FlatLaf.style", "arc: 12; background: $Background; border: 1,1,1,1,$Component.borderColor");
        upcomingContainer.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel upcomingHeader = new JLabel("Upcoming Bookings");
        upcomingHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        upcomingContainer.add(upcomingHeader, BorderLayout.NORTH);

        upcomingListPanel = new JPanel();
        upcomingListPanel.setLayout(new BoxLayout(upcomingListPanel, BoxLayout.Y_AXIS));
        upcomingListPanel.setOpaque(false);
        
        JScrollPane upcomingScrollPane = new JScrollPane(upcomingListPanel);
        upcomingScrollPane.setBorder(null);
        upcomingScrollPane.setOpaque(false);
        upcomingScrollPane.getViewport().setOpaque(false);
        upcomingContainer.add(upcomingScrollPane, BorderLayout.CENTER);
        
        centerPanel.add(upcomingContainer);

JPanel servicesContainer = new JPanel(new BorderLayout(10, 10));
        servicesContainer.putClientProperty("FlatLaf.style", "arc: 12; background: $Background; border: 1,1,1,1,$Component.borderColor");
        servicesContainer.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel servicesHeader = new JLabel("Offered Services");
        servicesHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        servicesContainer.add(servicesHeader, BorderLayout.NORTH);

        servicesListPanel = new JPanel();
        servicesListPanel.setLayout(new BoxLayout(servicesListPanel, BoxLayout.Y_AXIS));
        servicesListPanel.setOpaque(false);
        
        JScrollPane servicesScrollPane = new JScrollPane(servicesListPanel);
        servicesScrollPane.setBorder(null);
        servicesScrollPane.setOpaque(false);
        servicesScrollPane.getViewport().setOpaque(false);
        servicesContainer.add(servicesScrollPane, BorderLayout.CENTER);

        centerPanel.add(servicesContainer);
        
        add(centerPanel, BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout(15, 0));
        footer.putClientProperty("FlatLaf.style", "arc: 12; background: $Component.focusedBackground; border: 1,1,1,1,$Component.borderColor");
        footer.setBorder(new EmptyBorder(12, 20, 12, 20));

JLabel lblStatus = new JLabel("System Status: Active  •  Database Connected");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatus.setForeground(new Color(22, 163, 74));
        footer.add(lblStatus, BorderLayout.WEST);

JLabel lblInfo = new JLabel("Hours: Mon-Sat (9 AM - 6 PM)   |   Helpdesk: +91 98765 43210");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInfo.setForeground(Color.GRAY);
        lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
        footer.add(lblInfo, BorderLayout.CENTER);

JLabel lblVersion = new JLabel("App Version 1.0.0");
        lblVersion.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblVersion.setForeground(Color.GRAY);
        footer.add(lblVersion, BorderLayout.EAST);

        return footer;
    }

    private JPanel createMetricCard(String title, Color accentColor, JLabel valLabel) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.putClientProperty("FlatLaf.style", "arc: 12; background: $Background; border: 1,1,1,1,$Component.borderColor");
        card.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel content = new JPanel(new GridLayout(2, 1, 0, 2));
        content.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(Color.GRAY);
        content.add(lblTitle);

        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valLabel.setForeground(accentColor);
        content.add(valLabel);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    public void refreshData() {
        try {
            Map<String, Object> stats = appointmentDAO.getDashboardStats();
            totalBookingsVal.setText(String.valueOf(stats.getOrDefault("total_bookings", 0)));
            nextVal.setText(String.valueOf(stats.getOrDefault("next_bookings", 0)));

            Map<String, Integer> popularServices = appointmentDAO.getPopularServicesData();
            popularServicesChart.setChartData(popularServices);

            Map<String, Integer> staffWorkload = appointmentDAO.getStaffWorkloadData();
            staffWorkloadChart.setChartData(staffWorkload);

List<Service> services = serviceDAO.getAllServices();
            totalServicesVal.setText(String.valueOf(services.size()));
            
            servicesListPanel.removeAll();
            if (services.isEmpty()) {
                JLabel emptyLabel = new JLabel("No services available.");
                emptyLabel.setForeground(Color.GRAY);
                emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                servicesListPanel.add(emptyLabel);
            } else {
                for (Service service : services) {
                    servicesListPanel.add(createServiceItemPanel(service));
                    servicesListPanel.add(Box.createVerticalStrut(10));
                }
                servicesListPanel.add(Box.createVerticalGlue());
            }
            servicesListPanel.revalidate();
            servicesListPanel.repaint();

List<Appointment> allAppts = appointmentDAO.getAllAppointments();
            upcomingListPanel.removeAll();
            
            LocalDate today = LocalDate.now();
            List<Appointment> upcoming = new ArrayList<>();
            for (Appointment appt : allAppts) {
                if (!appt.getAppointmentDate().isBefore(today)) {
                    upcoming.add(appt);
                    if (upcoming.size() >= 5) break;
                }
            }

            if (upcoming.isEmpty()) {
                JLabel emptyLabel = new JLabel("No upcoming appointments found.");
                emptyLabel.setForeground(Color.GRAY);
                emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                upcomingListPanel.add(emptyLabel);
            } else {
                for (Appointment appt : upcoming) {
                    upcomingListPanel.add(createUpcomingItemPanel(appt));
                    upcomingListPanel.add(Box.createVerticalStrut(10));
                }
                upcomingListPanel.add(Box.createVerticalGlue());
            }

            upcomingListPanel.revalidate();
            upcomingListPanel.repaint();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private JPanel createUpcomingItemPanel(Appointment appt) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.putClientProperty("FlatLaf.style", "arc: 8; background: $Component.focusedBackground; border: 1,1,1,1,$Component.borderColor");
        panel.setBorder(new EmptyBorder(10, 12, 10, 12));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

JPanel dateTimePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        dateTimePanel.setOpaque(false);
        dateTimePanel.setPreferredSize(new Dimension(80, 45));
        JLabel dateLabel = new JLabel(DateTimeUtil.formatDateForDisplay(appt.getAppointmentDate()));
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel timeLabel = new JLabel(DateTimeUtil.formatTime(appt.getAppointmentTime()));
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        timeLabel.setForeground(Color.GRAY);
        dateTimePanel.add(dateLabel);
        dateTimePanel.add(timeLabel);
        panel.add(dateTimePanel, BorderLayout.WEST);

JPanel detailsPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        detailsPanel.setOpaque(false);
        JLabel clientLabel = new JLabel(appt.getClientName() + " — " + appt.getServiceType());
        clientLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JLabel staffLabel = new JLabel("Staff: " + appt.getAssignedStaff());
        staffLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        staffLabel.setForeground(Color.GRAY);
        detailsPanel.add(clientLabel);
        detailsPanel.add(staffLabel);
        panel.add(detailsPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createServiceItemPanel(Service service) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.putClientProperty("FlatLaf.style", "arc: 8; background: $Component.focusedBackground; border: 1,1,1,1,$Component.borderColor");
        panel.setBorder(new EmptyBorder(10, 12, 10, 12));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

JPanel priceDurationPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        priceDurationPanel.setOpaque(false);
        priceDurationPanel.setPreferredSize(new Dimension(80, 45));
        
        JLabel priceLabel = new JLabel("₹" + String.format("%.0f", service.getPrice()));
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        priceLabel.setForeground(new Color(22, 163, 74));
        priceLabel.setHorizontalAlignment(SwingConstants.LEFT);
        
        JLabel durationLabel = new JLabel(service.getDurationMinutes() + " mins");
        durationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        durationLabel.setForeground(Color.GRAY);
        durationLabel.setHorizontalAlignment(SwingConstants.LEFT);
        
        priceDurationPanel.add(priceLabel);
        priceDurationPanel.add(durationLabel);
        panel.add(priceDurationPanel, BorderLayout.WEST);

JPanel detailsPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        detailsPanel.setOpaque(false);
        JLabel nameLabel = new JLabel(service.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        String desc = service.getDescription();
        if (desc == null || desc.trim().isEmpty()) {
            desc = "No description available";
        } else if (desc.length() > 30) {
            desc = desc.substring(0, 27) + "...";
        }
        
        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLabel.setForeground(Color.GRAY);
        detailsPanel.add(nameLabel);
        detailsPanel.add(descLabel);
        panel.add(detailsPanel, BorderLayout.CENTER);

        return panel;
    }

    private static class ChartPanel extends JPanel {
        private final String title;
        private final Color barColor;
        private final JPanel barsContainer;

        public ChartPanel(String title, Color barColor) {
            this.title = title;
            this.barColor = barColor;
            putClientProperty("FlatLaf.style", "arc: 12; background: $Background; border: 1,1,1,1,$Component.borderColor");
            setBorder(new EmptyBorder(15, 15, 15, 15));
            setLayout(new BorderLayout(0, 10));
            
            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
            add(lblTitle, BorderLayout.NORTH);
            
            barsContainer = new JPanel();
            barsContainer.setLayout(new BoxLayout(barsContainer, BoxLayout.X_AXIS));
            barsContainer.setOpaque(false);

JScrollPane scrollPane = new JScrollPane(barsContainer);
            scrollPane.setBorder(null);
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            
            add(scrollPane, BorderLayout.CENTER);
        }

        public void setChartData(Map<String, Integer> data) {
            barsContainer.removeAll();

            if (data == null || data.isEmpty()) {
                JLabel emptyLabel = new JLabel("No data available.");
                emptyLabel.setForeground(Color.GRAY);
                emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
                barsContainer.add(emptyLabel);
            } else {
                int max = 0;
                for (int val : data.values()) {
                    if (val > max) max = val;
                }

                barsContainer.add(Box.createHorizontalGlue());
                for (Map.Entry<String, Integer> entry : data.entrySet()) {
                    barsContainer.add(createVerticalBarItem(entry.getKey(), entry.getValue(), max));
                    barsContainer.add(Box.createHorizontalStrut(12));
                }
                if (barsContainer.getComponentCount() > 1) {
                    barsContainer.remove(barsContainer.getComponentCount() - 1);
                }
                barsContainer.add(Box.createHorizontalGlue());
            }
            revalidate();
            repaint();
        }

        private JPanel createVerticalBarItem(String name, int val, int max) {
            JPanel barCol = new JPanel(new BorderLayout(0, 5));
            barCol.setOpaque(false);
            barCol.setPreferredSize(new Dimension(75, 140));
            barCol.setMinimumSize(new Dimension(75, 140));
            barCol.setMaximumSize(new Dimension(75, Integer.MAX_VALUE));

JLabel valLabel = new JLabel(String.valueOf(val));
            valLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            valLabel.setHorizontalAlignment(SwingConstants.CENTER);
            barCol.add(valLabel, BorderLayout.NORTH);

JPanel bar = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    int w = getWidth();
                    int h = getHeight();
                    double percent = max > 0 ? (double) val / max : 0.0;
                    int barHeight = (int) (h * percent);
                    if (barHeight < 5 && val > 0) barHeight = 5;

                    g2d.setColor(new Color(229, 231, 235));
                    g2d.fillRoundRect(w / 3, 0, w / 3, h, 6, 6);

                    g2d.setColor(barColor);
                    g2d.fillRoundRect(w / 3, h - barHeight, w / 3, barHeight, 6, 6);
                    
                    g2d.dispose();
                }
            };
            bar.setOpaque(false);
            barCol.add(bar, BorderLayout.CENTER);

String htmlName = "<html><center>" + name.replace(" ", "<br>") + "</center></html>";
            JLabel nameLabel = new JLabel(htmlName);
            nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            nameLabel.setForeground(Color.DARK_GRAY);
            nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
            nameLabel.setVerticalAlignment(SwingConstants.TOP);
            nameLabel.setPreferredSize(new Dimension(75, 48)); 
            barCol.add(nameLabel, BorderLayout.SOUTH);

            return barCol;
        }
    }
}

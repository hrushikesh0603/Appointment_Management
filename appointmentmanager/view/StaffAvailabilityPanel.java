package com.appointmentmanager.view;

import com.appointmentmanager.model.Appointment;
import com.appointmentmanager.service.AppointmentService;
import com.appointmentmanager.util.DateTimeUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.List;

public class StaffAvailabilityPanel extends JPanel {

    private JPanel gridPanel;
    private com.toedter.calendar.JDateChooser dateChooser;
    private JLabel dateLabel;
    private final AppointmentService appointmentService;

private static final Color BORDER_LIGHT = new Color(220, 220, 225);
    private static final Color BG_HEADER = new Color(240, 240, 243);
    private static final Color SLOT_AVAILABLE = new Color(220, 252, 231);
    private static final Color SLOT_BOOKED = new Color(254, 226, 226);
    private static final Color SLOT_AVAILABLE_BORDER = new Color(34, 197, 94);
    private static final Color SLOT_BOOKED_BORDER = new Color(239, 68, 68);
    private static final Color TEXT_PRIMARY = new Color(30, 30, 35);
    private static final Color TEXT_SECONDARY = new Color(100, 100, 110);
    private static final Color ACCENT_BLUE = new Color(37, 99, 235);

    public StaffAvailabilityPanel() {
        this.appointmentService = new AppointmentService();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(15, 20, 15, 20));

JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        topBar.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("Staff Availability");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        topBar.add(titleLabel);

        topBar.add(Box.createHorizontalStrut(20));

        JLabel selectLabel = new JLabel("Select Date:");
        selectLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        topBar.add(selectLabel);

        dateChooser = new com.toedter.calendar.JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setDate(new Date());
        dateChooser.setPreferredSize(new Dimension(150, 32));
        dateChooser.getCalendarButton().setBackground(ACCENT_BLUE);
        dateChooser.getCalendarButton().setForeground(Color.WHITE);
        dateChooser.getCalendarButton().putClientProperty("JButton.buttonType", "primary");
        dateChooser.addPropertyChangeListener("date", evt -> refreshAvailability());

JTextField dateTextField = (JTextField) dateChooser.getDateEditor().getUiComponent();
        dateTextField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dateTextField.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                dateChooser.getCalendarButton().doClick();
            }
        });
        topBar.add(dateChooser);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> refreshAvailability());
        topBar.add(refreshBtn);

        add(topBar, BorderLayout.NORTH);

gridPanel = new JPanel();
        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        legend.setBorder(new EmptyBorder(10, 0, 0, 0));
        legend.add(createLegend(SLOT_AVAILABLE, SLOT_AVAILABLE_BORDER, "Available"));
        legend.add(createLegend(SLOT_BOOKED, SLOT_BOOKED_BORDER, "Booked"));
        add(legend, BorderLayout.SOUTH);
    }

public void refreshAvailability() {
        Date selectedDate = dateChooser.getDate();
        if (selectedDate == null) {
            selectedDate = new Date();
        }

        LocalDate date = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        buildGrid(date);
    }

private void buildGrid(LocalDate date) {
        gridPanel.removeAll();

        String[] staffList = AppointmentService.STAFF_LIST;
        List<LocalTime> timeSlots = appointmentService.generateAllSlots();

        gridPanel.setLayout(new GridLayout(timeSlots.size() + 1, staffList.length + 1, 3, 3));

        JLabel corner = new JLabel("Time \\ Staff", SwingConstants.CENTER);
        corner.setFont(new Font("Segoe UI", Font.BOLD, 11));
        corner.setForeground(TEXT_SECONDARY);
        corner.setOpaque(true);
        corner.setBackground(BG_HEADER);
        corner.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        gridPanel.add(corner);

        for (String staff : staffList) {
            JLabel header = new JLabel(staff, SwingConstants.CENTER);
            header.setFont(new Font("Segoe UI", Font.BOLD, 11));
            header.setForeground(TEXT_PRIMARY);
            header.setOpaque(true);
            header.setBackground(BG_HEADER);
            header.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
            header.setPreferredSize(new Dimension(120, 36));
            gridPanel.add(header);
        }

        Map<String, Set<LocalTime>> bookedSlots = new HashMap<>();
        Map<String, Map<LocalTime, String>> bookedDetails = new HashMap<>();

        for (String staff : staffList) {
            bookedSlots.put(staff, new HashSet<>());
            bookedDetails.put(staff, new HashMap<>());
            try {
                List<Appointment> appointments = appointmentService.getBookedSlots(staff, date);
                for (Appointment apt : appointments) {
                    bookedSlots.get(staff).add(apt.getAppointmentTime());
                    bookedDetails.get(staff).put(apt.getAppointmentTime(),
                             apt.getClientName() + " (" + apt.getServiceType() + ")");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        for (LocalTime slot : timeSlots) {

            JLabel timeLabel = new JLabel(DateTimeUtil.formatTimeForDisplay(slot), SwingConstants.CENTER);
            timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            timeLabel.setForeground(TEXT_PRIMARY);
            timeLabel.setOpaque(true);
            timeLabel.setBackground(BG_HEADER);
            timeLabel.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
            gridPanel.add(timeLabel);

            for (String staff : staffList) {
                boolean isBooked = bookedSlots.get(staff).contains(slot);
                String detail = isBooked ? bookedDetails.get(staff).get(slot) : "Available";

                JPanel slotCell = createSlotCell(staff, slot, date, isBooked, detail);
                gridPanel.add(slotCell);
            }
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

private JPanel createSlotCell(String staff, LocalTime slot, LocalDate date, boolean isBooked, String detail) {
        JPanel cell = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bg = isBooked ? SLOT_BOOKED : SLOT_AVAILABLE;
                Color border = isBooked ? SLOT_BOOKED_BORDER : SLOT_AVAILABLE_BORDER;

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(border);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);

                g2.dispose();
            }
        };
        cell.setOpaque(false);
        cell.setPreferredSize(new Dimension(120, 36));

        JLabel label = new JLabel(isBooked ? "Booked" : "Open", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        label.setForeground(Color.BLACK);
        cell.add(label, BorderLayout.CENTER);

        cell.setToolTipText(detail);

        if (!isBooked) {
            cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            cell.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    MainFrame mainFrame = (MainFrame) SwingUtilities.getWindowAncestor(StaffAvailabilityPanel.this);
                    if (mainFrame != null) {
                        mainFrame.getController().showAddDialog(staff, date, slot);
                    }
                }
            });
        }

        return cell;
    }

private JPanel createLegend(Color bg, Color border, String text) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        JPanel swatch = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 4, 4);
                g2.dispose();
            }
        };
        swatch.setOpaque(false);
        swatch.setPreferredSize(new Dimension(20, 14));
        panel.add(swatch);

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        label.setForeground(TEXT_SECONDARY);
        panel.add(label);

        return panel;
    }
}

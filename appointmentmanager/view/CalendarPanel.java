package com.appointmentmanager.view;

import com.appointmentmanager.model.Appointment;
import com.appointmentmanager.service.AppointmentService;
import com.appointmentmanager.util.DateTimeUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;

public class CalendarPanel extends JPanel {

    private YearMonth currentMonth;
    private JPanel calendarGrid;
    private JLabel monthLabel;
    private final MainFrame mainFrame;
    private final AppointmentService appointmentService;
    private Map<LocalDate, List<Appointment>> appointmentsByDate;

private JPanel appointmentsPanel;
    private JLabel selectedDateLabel;
    private JPanel appointmentsListContainer;
    private JScrollPane appointmentsScrollPane;

private static final Color BORDER_LIGHT = new Color(220, 220, 225);
    private static final Color BG_DAY = new Color(250, 250, 250);
    private static final Color BG_DAY_HOVER = new Color(235, 235, 240);
    private static final Color BG_TODAY = new Color(219, 234, 254);
    private static final Color ACCENT = new Color(37, 99, 235);
    private static final Color TEXT_PRIMARY = new Color(30, 30, 35);
    private static final Color TEXT_SECONDARY = new Color(100, 100, 110);
    private static final Color DOT_GREEN = new Color(34, 197, 94);
    private static final Color DOT_YELLOW = new Color(234, 179, 8);
    private static final Color DOT_RED = new Color(239, 68, 68);

    public CalendarPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.appointmentService = new AppointmentService();
        this.currentMonth = YearMonth.now();
        this.appointmentsByDate = new HashMap<>();
        initializeUI();
        refreshCalendar();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(15, 20, 15, 20));

JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBorder(new EmptyBorder(0, 0, 15, 0));

        JButton prevBtn = createNavButton("Prev");
        prevBtn.addActionListener(e -> {
            currentMonth = currentMonth.minusMonths(1);
            refreshCalendar();
        });
        navBar.add(prevBtn, BorderLayout.WEST);

        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        monthLabel.setForeground(TEXT_PRIMARY);
        navBar.add(monthLabel, BorderLayout.CENTER);

        JButton nextBtn = createNavButton("Next");
        nextBtn.addActionListener(e -> {
            currentMonth = currentMonth.plusMonths(1);
            refreshCalendar();
        });
        navBar.add(nextBtn, BorderLayout.EAST);

        JButton todayBtn = createNavButton("Today");
        todayBtn.setPreferredSize(new Dimension(80, 32));

        JPanel rightNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        todayBtn.addActionListener(e -> {
            currentMonth = YearMonth.now();
            refreshCalendar();
        });
        rightNav.add(todayBtn);
        rightNav.add(nextBtn);
        navBar.add(rightNav, BorderLayout.EAST);

        add(navBar, BorderLayout.NORTH);

JPanel centerContainer = new JPanel(new BorderLayout(15, 0));
        centerContainer.setOpaque(false);

        calendarGrid = new JPanel();
        centerContainer.add(calendarGrid, BorderLayout.CENTER);

appointmentsPanel = createAppointmentsPanel();
        centerContainer.add(appointmentsPanel, BorderLayout.EAST);

        add(centerContainer, BorderLayout.CENTER);

JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        legend.setBorder(new EmptyBorder(10, 0, 0, 0));
        legend.add(createLegendDot(DOT_GREEN, "1-2 appointments"));
        legend.add(createLegendDot(DOT_YELLOW, "3-4 appointments"));
        legend.add(createLegendDot(DOT_RED, "5+ appointments"));
        add(legend, BorderLayout.SOUTH);
    }

public void refreshCalendar() {
        if (appointmentsPanel != null) {
            appointmentsPanel.setVisible(false);
        }

        monthLabel.setText(currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())
                + " " + currentMonth.getYear());

LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();

        appointmentsByDate.clear();
        try {
            List<Appointment> appointments = appointmentService.getAppointmentsForDateRange(startDate, endDate);
            for (Appointment apt : appointments) {
                appointmentsByDate
                        .computeIfAbsent(apt.getAppointmentDate(), k -> new ArrayList<>())
                        .add(apt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        rebuildGrid();
    }

private void rebuildGrid() {
        calendarGrid.removeAll();
        calendarGrid.setLayout(new GridLayout(0, 7, 4, 4));

String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : dayNames) {
            JLabel label = new JLabel(day, SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setForeground(TEXT_SECONDARY);
            label.setPreferredSize(new Dimension(0, 30));
            calendarGrid.add(label);
        }

LocalDate firstDay = currentMonth.atDay(1);
        int startDayOfWeek = firstDay.getDayOfWeek().getValue() % 7; 

for (int i = 0; i < startDayOfWeek; i++) {
            JPanel emptyCell = new JPanel();
            calendarGrid.add(emptyCell);
        }

int daysInMonth = currentMonth.lengthOfMonth();
        LocalDate today = LocalDate.now();

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            List<Appointment> dayAppointments = appointmentsByDate.getOrDefault(date, Collections.emptyList());
            int appointmentCount = dayAppointments.size();

            JPanel dayCell = createDayCell(day, date, today, appointmentCount);
            calendarGrid.add(dayCell);
        }

int totalCells = startDayOfWeek + daysInMonth;
        int remainingCells = (7 - (totalCells % 7)) % 7;
        for (int i = 0; i < remainingCells; i++) {
            JPanel emptyCell = new JPanel();
            calendarGrid.add(emptyCell);
        }

        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

private JPanel createDayCell(int day, LocalDate date, LocalDate today, int appointmentCount) {
        JPanel cell = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

Color bg = date.equals(today) ? BG_TODAY : getBackground();
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

if (date.equals(today)) {
                    g2.setColor(ACCENT);
                    g2.setStroke(new BasicStroke(2));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 8, 8);
                }

if (appointmentCount > 0) {
                    Color dotColor;
                    if (appointmentCount <= 2) dotColor = DOT_GREEN;
                    else if (appointmentCount <= 4) dotColor = DOT_YELLOW;
                    else dotColor = DOT_RED;

                    int dotSize = 8;
                    int dotX = (getWidth() - dotSize) / 2;
                    int dotY = getHeight() - dotSize - 8;
                    g2.setColor(dotColor);
                    g2.fillOval(dotX, dotY, dotSize, dotSize);
                }

                g2.dispose();
            }
        };

        cell.setBackground(BG_DAY);
        cell.setOpaque(false);
        cell.setPreferredSize(new Dimension(0, 70));
        cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

JLabel dayLabel = new JLabel(String.valueOf(day), SwingConstants.CENTER);
        dayLabel.setFont(new Font("Segoe UI", date.equals(today) ? Font.BOLD : Font.PLAIN, 15));
        dayLabel.setForeground(date.equals(today) ? ACCENT : TEXT_PRIMARY);
        dayLabel.setBorder(new EmptyBorder(8, 0, 0, 0));
        cell.add(dayLabel, BorderLayout.NORTH);

if (appointmentCount > 0) {
            JLabel countLabel = new JLabel(appointmentCount + " appt" + (appointmentCount > 1 ? "s" : ""),
                    SwingConstants.CENTER);
            countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            countLabel.setForeground(TEXT_SECONDARY);
            cell.add(countLabel, BorderLayout.CENTER);
        }

cell.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (mainFrame.getController() != null) {
                    mainFrame.getController().filterByDate(date);
                }
                showDailyAppointments(date);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                cell.setBackground(BG_DAY_HOVER);
                cell.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                cell.setBackground(BG_DAY);
                cell.repaint();
            }
        });

        return cell;
    }

private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

private JPanel createLegendDot(Color color, String text) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(2, 2, 10, 10);
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(14, 14));
        panel.add(dot);

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        label.setForeground(TEXT_SECONDARY);
        panel.add(label);

        return panel;
    }

    private JPanel createAppointmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setPreferredSize(new Dimension(380, 0)); 
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER_LIGHT),
                new EmptyBorder(0, 15, 0, 0)
        ));
        panel.setOpaque(false);
        panel.setVisible(false); 

selectedDateLabel = new JLabel("Appointments");
        selectedDateLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        selectedDateLabel.setForeground(TEXT_PRIMARY);
        panel.add(selectedDateLabel, BorderLayout.NORTH);

appointmentsListContainer = new JPanel();
        appointmentsListContainer.setLayout(new BoxLayout(appointmentsListContainer, BoxLayout.Y_AXIS));
        appointmentsListContainer.setBackground(Color.WHITE);
        appointmentsListContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        appointmentsScrollPane = new JScrollPane(appointmentsListContainer);
        appointmentsScrollPane.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        appointmentsScrollPane.getVerticalScrollBar().setUnitIncrement(12);
        panel.add(appointmentsScrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void showDailyAppointments(LocalDate date) {
        selectedDateLabel.setText("Appointments for " + DateTimeUtil.formatDateForDisplay(date));
        appointmentsListContainer.removeAll();

        List<Appointment> dayAppts = appointmentsByDate.get(date);
        if (dayAppts == null) {
            dayAppts = new ArrayList<>();
        } else {
            
            dayAppts.sort(Comparator.comparing(Appointment::getAppointmentTime));
        }

        if (dayAppts.isEmpty()) {
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setOpaque(false);
            JLabel emptyLabel = new JLabel("No appointments for this day.");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            emptyLabel.setForeground(TEXT_SECONDARY);
            emptyPanel.add(emptyLabel);
            appointmentsListContainer.add(emptyPanel);
        } else {
            for (Appointment apt : dayAppts) {
                JPanel card = createAppointmentCard(apt);
                appointmentsListContainer.add(card);
                appointmentsListContainer.add(Box.createVerticalStrut(10));
            }
        }

        appointmentsPanel.setVisible(true);
        revalidate();
        repaint();
    }

    private JPanel createAppointmentCard(Appointment apt) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBackground(new Color(248, 249, 250));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                new EmptyBorder(10, 12, 10, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel timeLabel = new JLabel(DateTimeUtil.formatTime(apt.getAppointmentTime()));
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        timeLabel.setForeground(ACCENT);
        topRow.add(timeLabel, BorderLayout.WEST);
        card.add(topRow, BorderLayout.NORTH);

JPanel bodyRow = new JPanel(new GridLayout(2, 1, 2, 2));
        bodyRow.setOpaque(false);

        JLabel clientLabel = new JLabel(apt.getClientName() + " — " + apt.getServiceType());
        clientLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        clientLabel.setForeground(TEXT_PRIMARY);
        bodyRow.add(clientLabel);

        JLabel staffLabel = new JLabel("Staff: " + apt.getAssignedStaff());
        staffLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        staffLabel.setForeground(TEXT_SECONDARY);
        bodyRow.add(staffLabel);

        card.add(bodyRow, BorderLayout.CENTER);

        return card;
    }
}

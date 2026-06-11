package com.appointmentmanager.view;

import com.appointmentmanager.controller.AppointmentController;
import com.appointmentmanager.model.Appointment;
import com.appointmentmanager.model.User;
import com.appointmentmanager.util.DateTimeUtil;
import com.appointmentmanager.service.EmailReminderScheduler;
import com.appointmentmanager.service.AppointmentAutoCompleteScheduler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class MainFrame extends JFrame {

private DashboardPanel dashboardPanel;
    private ClientsPanel clientsPanel;
    private CalendarPanel calendarPanel;
    private StaffAvailabilityPanel staffAvailabilityPanel;
    private FeedbackPanel feedbackPanel;

private JTable appointmentTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> searchFieldCombo;
    private JTabbedPane tabbedPane;

private AppointmentController controller;

private final User currentUser;

private static final Color BG_LIGHT = new Color(245, 245, 247);
    private static final Color BORDER_LIGHT = new Color(220, 220, 225);
    private static final Color ACCENT_BLUE = new Color(37, 99, 235);
    private static final Color ACCENT_GREEN = new Color(22, 163, 74);
    private static final Color ACCENT_RED = new Color(220, 38, 38);
    private static final Color ACCENT_YELLOW = new Color(202, 138, 4);

private static final String[] COLUMN_NAMES = {
            "ID", "Client Name", "Contact", "Email", "Date", "Time", "Service", "Staff", "Remarks"
    };

    public MainFrame(User user) {
        this.currentUser = user;
        initializeUI();
        this.controller = new AppointmentController(this);
        controller.refreshTable();

EmailReminderScheduler.getInstance().start();

AppointmentAutoCompleteScheduler.getInstance().setMainFrame(this);
        AppointmentAutoCompleteScheduler.getInstance().start();
    }

    private void initializeUI() {
        setTitle("Appointment Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1250, 780);
        setMinimumSize(new Dimension(950, 650));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_LIGHT);
        setLayout(new BorderLayout(0, 0));

addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                EmailReminderScheduler.getInstance().stop();
                AppointmentAutoCompleteScheduler.getInstance().stop();
            }
        });

add(createTopBar(), BorderLayout.NORTH);

tabbedPane = createTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(15, 0));
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_LIGHT),
                new EmptyBorder(12, 20, 12, 20)
        ));

JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);
        JLabel appTitle = new JLabel("Appointment Management System");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titlePanel.add(appTitle);

        JLabel userBadge = new JLabel("  " + currentUser.getUsername().toUpperCase());
        userBadge.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        userBadge.setForeground(ACCENT_BLUE);
        titlePanel.add(userBadge);
        topBar.add(titlePanel, BorderLayout.WEST);

JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setOpaque(false);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutBtn.setBackground(ACCENT_RED);
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.putClientProperty("JButton.buttonType", "primary");
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setPreferredSize(new Dimension(110, 34));
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to logout?",
                    "Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                EmailReminderScheduler.getInstance().stop();
                AppointmentAutoCompleteScheduler.getInstance().stop();
                dispose();
                SwingUtilities.invokeLater(() -> {
                    LoginFrame loginFrame = new LoginFrame();
                    loginFrame.setVisible(true);
                });
            }
        });
        rightPanel.add(logoutBtn);
        topBar.add(rightPanel, BorderLayout.EAST);

        return topBar;
    }

private JTabbedPane createTabbedPane() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));

dashboardPanel = new DashboardPanel();
        tabs.addTab("Dashboard", dashboardPanel);

JPanel tablePanel = createTablePanel();
        tabs.addTab("All Bookings", tablePanel);

calendarPanel = new CalendarPanel(this);
        tabs.addTab("Calendar View", calendarPanel);

staffAvailabilityPanel = new StaffAvailabilityPanel();
        tabs.addTab("Staff Availability", staffAvailabilityPanel);

clientsPanel = new ClientsPanel();
        tabs.addTab("Clients Directory", clientsPanel);

feedbackPanel = new FeedbackPanel();
        tabs.addTab("Feedback", feedbackPanel);

tabs.addChangeListener(e -> {
            int selectedIndex = tabs.getSelectedIndex();
            if (selectedIndex == 0) {
                dashboardPanel.refreshData();
            } else if (selectedIndex == 1) {
                controller.refreshTable();
            } else if (selectedIndex == 2) {
                calendarPanel.refreshCalendar();
            } else if (selectedIndex == 3) {
                staffAvailabilityPanel.refreshAvailability();
            } else if (selectedIndex == 4) {
                clientsPanel.loadClients();
            } else if (selectedIndex == 5) {
                feedbackPanel.refreshFeedbacks();
            }
        });

        return tabs;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 15, 10, 15));

JPanel controlsPanel = new JPanel(new BorderLayout(15, 0));
        controlsPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        controlsPanel.setOpaque(false);

JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchPanel.setOpaque(false);

        searchFieldCombo = new JComboBox<>(new String[]{
                "Client Name", "Contact Number", "Client Email", "Appointment Date", "Service Type", "Assigned Staff"
        });
        searchFieldCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchFieldCombo.setPreferredSize(new Dimension(150, 34));
        searchPanel.add(searchFieldCombo);

        searchField = new JTextField(15);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
        searchField.setPreferredSize(new Dimension(200, 34));
        searchField.putClientProperty("JTextField.placeholderText", "Search bookings...");
        searchPanel.add(searchField);

        JButton searchBtn = createActionButton("Search", ACCENT_BLUE);
        searchBtn.addActionListener(e -> {
            controller.searchAppointments();
        });
        searchPanel.add(searchBtn);

        JButton clearBtn = createActionButton("Clear", null);
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            controller.refreshTable();
        });
        searchPanel.add(clearBtn);
        controlsPanel.add(searchPanel, BorderLayout.WEST);

JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionPanel.setOpaque(false);

        JButton addBtn = createActionButton("Add", ACCENT_GREEN);
        addBtn.addActionListener(e -> controller.showAddDialog());
        actionPanel.add(addBtn);

        JButton editBtn = createActionButton("Edit", ACCENT_YELLOW);
        editBtn.addActionListener(e -> controller.showEditDialog());
        actionPanel.add(editBtn);

        JButton cancelBtn = createActionButton("Cancel", ACCENT_RED);
        cancelBtn.addActionListener(e -> controller.cancelAppointment());
        actionPanel.add(cancelBtn);

        JButton refreshBtn = createActionButton("Refresh", null);
        refreshBtn.addActionListener(e -> {
            controller.refreshTable();
        });
        actionPanel.add(refreshBtn);

JButton exportBtn = createActionButton("Export", new Color(2, 132, 199));
        JPopupMenu exportMenu = new JPopupMenu();

        JMenuItem csvItem = new JMenuItem("Export as CSV");
        csvItem.addActionListener(ev -> controller.exportToCSV());
        exportMenu.add(csvItem);

        JMenuItem pdfItem = new JMenuItem("Export as PDF");
        pdfItem.addActionListener(ev -> controller.exportToPDF());
        exportMenu.add(pdfItem);

        exportBtn.addActionListener(ev ->
                exportMenu.show(exportBtn, 0, exportBtn.getHeight()));
        actionPanel.add(exportBtn);

        controlsPanel.add(actionPanel, BorderLayout.EAST);
        panel.add(controlsPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        appointmentTable = new JTable(tableModel);
        appointmentTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        appointmentTable.setRowHeight(36);
        appointmentTable.setGridColor(BORDER_LIGHT);
        appointmentTable.setShowGrid(true);
        appointmentTable.setIntercellSpacing(new Dimension(0, 1));
        appointmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        appointmentTable.setRowSorter(sorter);

        JTableHeader header = appointmentTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(0, 40));

DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                }
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        };

        for (int i = 0; i < appointmentTable.getColumnCount(); i++) {
            appointmentTable.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

appointmentTable.getColumnModel().getColumn(0).setPreferredWidth(40);   
        appointmentTable.getColumnModel().getColumn(1).setPreferredWidth(140);  
        appointmentTable.getColumnModel().getColumn(2).setPreferredWidth(100);  
        appointmentTable.getColumnModel().getColumn(3).setPreferredWidth(160);  
        appointmentTable.getColumnModel().getColumn(4).setPreferredWidth(90);   
        appointmentTable.getColumnModel().getColumn(5).setPreferredWidth(60);   
        appointmentTable.getColumnModel().getColumn(6).setPreferredWidth(100);  
        appointmentTable.getColumnModel().getColumn(7).setPreferredWidth(110);  
        appointmentTable.getColumnModel().getColumn(8).setPreferredWidth(120);  

        appointmentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    controller.showEditDialog();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JButton createActionButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        if (bgColor != null) {
            button.setBackground(bgColor);
            button.setForeground(Color.WHITE);
            button.putClientProperty("JButton.buttonType", "primary");
        }
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 34));
        return button;
    }

public void updateTable(List<Appointment> appointments) {
        tableModel.setRowCount(0);
        for (Appointment apt : appointments) {
            tableModel.addRow(new Object[]{
                    apt.getId(),
                    apt.getClientName(),
                    apt.getContactNumber(),
                    apt.getClientEmail() != null ? apt.getClientEmail() : "",
                    DateTimeUtil.formatDate(apt.getAppointmentDate()),
                    DateTimeUtil.formatTime(apt.getAppointmentTime()),
                    apt.getServiceType(),
                    apt.getAssignedStaff(),
                    apt.getRemarks()
            });
        }

if (dashboardPanel != null) {
            dashboardPanel.refreshData();
        }
    }

    public int getSelectedAppointmentId() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow == -1) {
            return -1;
        }
        int modelRow = appointmentTable.convertRowIndexToModel(selectedRow);
        return (int) tableModel.getValueAt(modelRow, 0);
    }

    public String getSearchText() {
        return searchField.getText().trim();
    }

    public String getSearchField() {
        return (String) searchFieldCombo.getSelectedItem();
    }

public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public boolean showConfirm(String message) {
        return JOptionPane.showConfirmDialog(this, message, "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public CalendarPanel getCalendarPanel() {
        return calendarPanel;
    }

    public StaffAvailabilityPanel getStaffAvailabilityPanel() {
        return staffAvailabilityPanel;
    }

    public ClientsPanel getClientsPanel() {
        return clientsPanel;
    }

    public AppointmentController getController() {
        return controller;
    }
}

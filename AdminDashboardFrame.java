package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

import database.DatabaseConnection;

public class AdminDashboardFrame extends JFrame {

    private JComboBox<EventItem> eventCombo;
    private JTable attendanceTable;
    private DefaultTableModel tableModel;

    public AdminDashboardFrame(String adminName) {

        setTitle("SmartCampus - Admin Dashboard");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(35, 50, 80));
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel heading = new JLabel("SMARTCAMPUS ADMIN PANEL");
        heading.setFont(new Font("Arial", Font.BOLD, 25));
        heading.setForeground(Color.WHITE);

        JLabel welcome = new JLabel("Welcome, " + adminName + "!");
        welcome.setFont(new Font("Arial", Font.PLAIN, 16));
        welcome.setForeground(Color.WHITE);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 5, 5));
        titlePanel.setOpaque(false);
        titlePanel.add(heading);
        titlePanel.add(welcome);

        JButton logoutButton = new JButton("Logout");

        header.add(titlePanel, BorderLayout.WEST);
        header.add(logoutButton, BorderLayout.EAST);

        // TABS
        JTabbedPane tabs = new JTabbedPane();

        // ATTENDANCE GUI
        JPanel attendancePanel = new JPanel(new BorderLayout(15, 15));
        attendancePanel.setBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        );

        JLabel attendanceHeading =
                new JLabel("ATTENDANCE MANAGEMENT");

        attendanceHeading.setFont(
                new Font("Arial", Font.BOLD, 23)
        );

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        eventCombo = new JComboBox<>();

        JButton loadButton = new JButton("Load Students");
        JButton saveButton = new JButton("Save Attendance");

        topPanel.add(new JLabel("Select Event:"));
        topPanel.add(eventCombo);
        topPanel.add(loadButton);
        topPanel.add(saveButton);

        // TABLE
        tableModel = new DefaultTableModel(
                new String[]{
                    "Student ID",
                    "Roll Number",
                    "Student Name",
                    "Attendance"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };

        attendanceTable = new JTable(tableModel);

        JComboBox<String> statusCombo =
                new JComboBox<>(new String[]{"Present", "Absent"});

        attendanceTable.getColumnModel()
                .getColumn(3)
                .setCellEditor(new DefaultCellEditor(statusCombo));

        JPanel attendanceTop = new JPanel(new BorderLayout(10, 10));

        attendanceTop.add(attendanceHeading, BorderLayout.NORTH);
        attendanceTop.add(topPanel, BorderLayout.CENTER);

        attendancePanel.add(attendanceTop, BorderLayout.NORTH);

        attendancePanel.add(
                new JScrollPane(attendanceTable),
                BorderLayout.CENTER
        );

        tabs.addTab("Attendance", attendancePanel);

        // LOAD EVENTS
        loadEvents();

        // LOAD STUDENTS BUTTON
        loadButton.addActionListener(e -> loadStudents());

        // SAVE ATTENDANCE BUTTON
        saveButton.addActionListener(e -> saveAttendance());

        // LOGOUT
        logoutButton.addActionListener(e -> {

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to logout?",
                    "Logout",
                    JOptionPane.YES_NO_OPTION
            );

            if (choice == JOptionPane.YES_OPTION) {
                dispose();
                new RoleSelectFrame().setVisible(true);
            }
        });

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(tabs, BorderLayout.CENTER);

        add(mainPanel);

        setVisible(true);
    }

    // EVENT ITEM
    private static class EventItem {

        int id;
        String name;

        EventItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return id + " - " + name;
        }
    }

    // LOAD EVENTS
    private void loadEvents() {

        eventCombo.removeAllItems();

        String sql = "SELECT event_id, event_name FROM events";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                eventCombo.addItem(
                        new EventItem(
                                rs.getInt("event_id"),
                                rs.getString("event_name")
                        )
                );
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error loading events: " + e.getMessage()
            );
        }
    }

    // LOAD BOOKED STUDENTS
    private void loadStudents() {

        EventItem selected =
                (EventItem) eventCombo.getSelectedItem();

        if (selected == null) {
            JOptionPane.showMessageDialog(
                    this, "Please select an event."
            );
            return;
        }

        tableModel.setRowCount(0);

        String sql =
                "SELECT s.student_id, s.roll_number, s.full_name, "
                + "a.status "
                + "FROM bookings b "
                + "JOIN students s ON b.student_id = s.student_id "
                + "LEFT JOIN attendance a "
                + "ON a.student_id = s.student_id "
                + "AND a.event_id = b.event_id "
                + "WHERE b.event_id = ? "
                + "AND b.booking_status = 'Booked'";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, selected.id);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    String status = rs.getString("status");

                    if (status == null) {
                        status = "Absent";
                    }

                    tableModel.addRow(new Object[]{
                        rs.getInt("student_id"),
                        rs.getString("roll_number"),
                        rs.getString("full_name"),
                        status
                    });
                }
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "No booked students found."
                );
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error loading students: " + e.getMessage()
            );
        }
    }

    // SAVE ATTENDANCE
    private void saveAttendance() {

        EventItem selected =
                (EventItem) eventCombo.getSelectedItem();

        if (selected == null) {
            JOptionPane.showMessageDialog(
                    this, "Please select an event."
            );
            return;
        }

        if (attendanceTable.isEditing()) {
            attendanceTable.getCellEditor().stopCellEditing();
        }

        String checkSql =
                "SELECT attendance_id FROM attendance "
                + "WHERE student_id = ? AND event_id = ?";

        String updateSql =
                "UPDATE attendance SET status = ? "
                + "WHERE attendance_id = ?";

        String insertSql =
                "INSERT INTO attendance "
                + "(student_id, event_id, status) "
                + "VALUES (?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection()) {

            con.setAutoCommit(false);

            try {

                for (int i = 0; i < tableModel.getRowCount(); i++) {

                    int studentId = Integer.parseInt(
                            tableModel.getValueAt(i, 0).toString()
                    );

                    String status =
                            tableModel.getValueAt(i, 3).toString();

                    int attendanceId = -1;

                    try (PreparedStatement check =
                            con.prepareStatement(checkSql)) {

                        check.setInt(1, studentId);
                        check.setInt(2, selected.id);

                        try (ResultSet rs = check.executeQuery()) {

                            if (rs.next()) {
                                attendanceId =
                                        rs.getInt("attendance_id");
                            }
                        }
                    }

                    if (attendanceId != -1) {

                        try (PreparedStatement update =
                                con.prepareStatement(updateSql)) {

                            update.setString(1, status);
                            update.setInt(2, attendanceId);
                            update.executeUpdate();
                        }

                    } else {

                        try (PreparedStatement insert =
                                con.prepareStatement(insertSql)) {

                            insert.setInt(1, studentId);
                            insert.setInt(2, selected.id);
                            insert.setString(3, status);
                            insert.executeUpdate();
                        }
                    }
                }

                con.commit();

                JOptionPane.showMessageDialog(
                        this,
                        "Attendance saved successfully!"
                );

            } catch (Exception e) {

                con.rollback();
                throw e;
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error saving attendance: " + e.getMessage()
            );
        }
    }
}
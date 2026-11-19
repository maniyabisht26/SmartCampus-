package attendance;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import database.DatabaseConnection;

public class StudentAttendanceFrame extends JFrame {

    private int studentId;
    private JTable attendanceTable;
    private DefaultTableModel tableModel;

    public StudentAttendanceFrame(int studentId) {

        this.studentId = studentId;

        setTitle("SmartCampus - My Attendance");
        setSize(650, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);

        // Heading
        JLabel heading = new JLabel("MY ATTENDANCE", JLabel.CENTER);

        heading.setFont(new Font("Arial", Font.BOLD, 24));
        heading.setForeground(new Color(30, 60, 120));

        heading.setBorder(
                BorderFactory.createEmptyBorder(20, 10, 20, 10)
        );

        // Table
        String[] columns = {
            "Event Name",
            "Event Date",
            "Attendance Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        attendanceTable = new JTable(tableModel);

        attendanceTable.setRowHeight(30);
        attendanceTable.setFont(new Font("Arial", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(attendanceTable);

        // Back Button
        JButton backButton = new JButton("Close");

        backButton.addActionListener(e -> dispose());

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(backButton);

        // Add Components
        mainPanel.add(heading, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Load Attendance Records
        loadAttendance();

        setVisible(true);
    }

    private void loadAttendance() {

        String sql = "SELECT e.event_name, e.event_date, a.status "
                + "FROM attendance a "
                + "JOIN events e ON a.event_id = e.event_id "
                + "WHERE a.student_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {

                boolean found = false;

                while (rs.next()) {

                    found = true;

                    String eventName = rs.getString("event_name");
                    String eventDate = rs.getDate("event_date").toString();
                    String status = rs.getString("status");

                    tableModel.addRow(new Object[]{
                        eventName,
                        eventDate,
                        status
                    });
                }

                if (!found) {

                    JOptionPane.showMessageDialog(
                            this,
                            "No attendance records found.",
                            "My Attendance",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load attendance records.\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
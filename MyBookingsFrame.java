
package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import database.DatabaseConnection;

public class MyBookingsFrame extends JFrame {

    private final int studentId;
    private JTable bookingTable;
    private DefaultTableModel tableModel;

    public MyBookingsFrame(int studentId) {
        this.studentId = studentId;

        setTitle("SmartCampus - My Bookings");
        setSize(850, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel heading = new JLabel("My Booked Events");
        heading.setFont(new Font("Arial", Font.BOLD, 24));
        heading.setForeground(new Color(25, 48, 90));

        mainPanel.add(heading, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new String[]{
                    "Booking ID", "Event Name", "Date",
                    "Time", "Venue", "Booking Status"
                }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bookingTable = new JTable(tableModel);
        bookingTable.setRowHeight(28);

        mainPanel.add(new JScrollPane(bookingTable),
                BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh Bookings");
        refreshButton.addActionListener(e -> loadBookings());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        loadBookings();
        setVisible(true);
    }

    private void loadBookings() {

        tableModel.setRowCount(0);

        String sql = "SELECT b.booking_id, e.event_name, "
                + "e.event_date, e.event_time, e.venue, "
                + "b.booking_status "
                + "FROM bookings b "
                + "JOIN events e ON b.event_id = e.event_id "
                + "WHERE b.student_id = ? "
                + "ORDER BY b.booking_date DESC";

        try (Connection con = DatabaseConnection.getConnection()) {

            if (con == null) {
                JOptionPane.showMessageDialog(
                        this, "Database connection failed.");
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, studentId);

                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        tableModel.addRow(new Object[]{
                            rs.getInt("booking_id"),
                            rs.getString("event_name"),
                            rs.getDate("event_date"),
                            rs.getTime("event_time"),
                            rs.getString("venue"),
                            rs.getString("booking_status")
                        });
                    }
                }
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "You have not booked any events yet.");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this, "Could not load bookings:\n"
                            + ex.getMessage());
        }
    }
}


package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import database.DatabaseConnection;

public class EventsFrame extends JFrame {

    private final int studentId;
    private JTable eventTable;
    private DefaultTableModel tableModel;

    public EventsFrame(int studentId) {
        this.studentId = studentId;

        setTitle("SmartCampus - Available Events");
        setSize(850, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel heading = new JLabel("Available Campus Events");
        heading.setFont(new Font("Arial", Font.BOLD, 24));
        heading.setForeground(new Color(25, 48, 90));

        mainPanel.add(heading, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new String[]{
                    "Event ID", "Event Name", "Date",
                    "Time", "Venue", "Available Seats"
                }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        eventTable = new JTable(tableModel);
        eventTable.setRowHeight(28);
        eventTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        mainPanel.add(new JScrollPane(eventTable),
                BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton refreshButton = new JButton("Refresh Events");
        JButton bookButton = new JButton("Book Selected Event");

        refreshButton.addActionListener(e -> loadEvents());
        bookButton.addActionListener(e -> bookEvent());

        buttonPanel.add(refreshButton);
        buttonPanel.add(bookButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        loadEvents();
        setVisible(true);
    }

    private void loadEvents() {
        tableModel.setRowCount(0);

        String sql = "SELECT event_id, event_name, event_date, "
                + "event_time, venue, available_seats "
                + "FROM events ORDER BY event_date";

        try (Connection con = DatabaseConnection.getConnection()) {

            if (con == null) {
                JOptionPane.showMessageDialog(
                        this, "Database connection failed.");
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                        rs.getInt("event_id"),
                        rs.getString("event_name"),
                        rs.getDate("event_date"),
                        rs.getTime("event_time"),
                        rs.getString("venue"),
                        rs.getInt("available_seats")
                    });
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this, "Could not load events:\n"
                            + ex.getMessage());
        }
    }

    private void bookEvent() {

        int selectedRow = eventTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this, "Please select an event first.");
            return;
        }

        int eventId = (int) tableModel.getValueAt(
                selectedRow, 0);

        int seats = (int) tableModel.getValueAt(
                selectedRow, 5);

        if (seats <= 0) {
            JOptionPane.showMessageDialog(
                    this, "Sorry, this event is fully booked.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Do you want to book "
                        + tableModel.getValueAt(selectedRow, 1)
                        + "?",
                "Confirm Booking",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String checkSql =
                "SELECT booking_id FROM bookings "
                + "WHERE student_id = ? AND event_id = ?";

        String seatSql =
                "UPDATE events SET available_seats = "
                + "available_seats - 1 "
                + "WHERE event_id = ? AND available_seats > 0";

        String insertSql =
                "INSERT INTO bookings (student_id, event_id) "
                + "VALUES (?, ?)";

        try (Connection con = DatabaseConnection.getConnection()) {

            if (con == null) {
                JOptionPane.showMessageDialog(
                        this, "Database connection failed.");
                return;
            }

            con.setAutoCommit(false);

            try {
                try (PreparedStatement check =
                             con.prepareStatement(checkSql)) {

                    check.setInt(1, studentId);
                    check.setInt(2, eventId);

                    try (ResultSet rs = check.executeQuery()) {
                        if (rs.next()) {
                            con.rollback();
                            JOptionPane.showMessageDialog(
                                    this,
                                    "You have already booked this event.");
                            return;
                        }
                    }
                }

                try (PreparedStatement seat =
                             con.prepareStatement(seatSql)) {

                    seat.setInt(1, eventId);

                    if (seat.executeUpdate() == 0) {
                        con.rollback();
                        JOptionPane.showMessageDialog(
                                this,
                                "No seats are available.");
                        return;
                    }
                }

                try (PreparedStatement insert =
                             con.prepareStatement(insertSql)) {

                    insert.setInt(1, studentId);
                    insert.setInt(2, eventId);
                    insert.executeUpdate();
                }

                con.commit();

                JOptionPane.showMessageDialog(
                        this, "Event booked successfully!");

                loadEvents();

            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this, "Booking failed:\n"
                            + ex.getMessage());
        }
    }
}

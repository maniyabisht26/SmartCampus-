package payment;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

import database.DatabaseConnection;

public class PaymentFrame extends JFrame {

    private int bookingId;
    private int eventId;
    private double amount;

    private JComboBox<String> paymentMethod;

    public PaymentFrame(int bookingId, int eventId, double amount) {

        this.bookingId = bookingId;
        this.eventId = eventId;
        this.amount = amount;

        setTitle("SmartCampus - Dummy Payment");

        setSize(400, 380);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLocationRelativeTo(null);

        JPanel panel = new JPanel();

        panel.setLayout(new GridLayout(0, 1, 10, 10));

        panel.setBorder(
                BorderFactory.createEmptyBorder(25, 35, 25, 35)
        );

        JLabel heading = new JLabel(
                "SMARTCAMPUS PAYMENT",
                SwingConstants.CENTER
        );

        heading.setFont(
                new Font("Arial", Font.BOLD, 20)
        );

        heading.setForeground(new Color(25, 48, 90));

        JLabel bookingLabel = new JLabel(
                "Booking ID: " + bookingId
        );

        JLabel amountLabel = new JLabel(
                "Amount: Rs. " + amount
        );

        JLabel methodLabel = new JLabel(
                "Select Payment Method:"
        );

        String[] methods = {
            "UPI",
            "Debit/Credit Card",
            "Net Banking"
        };

        paymentMethod = new JComboBox<>(methods);

        JButton payButton = new JButton("Pay Now");

        JButton cancelButton = new JButton("Cancel");

        panel.add(heading);
        panel.add(bookingLabel);
        panel.add(amountLabel);
        panel.add(methodLabel);
        panel.add(paymentMethod);
        panel.add(payButton);
        panel.add(cancelButton);

        payButton.addActionListener(e -> makePayment());

        cancelButton.addActionListener(e -> cancelPayment());

        add(panel);

        setVisible(true);
    }

    // Simulate payment and save details
    private void makePayment() {

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Do you want to make a dummy payment of Rs. "
                + amount + "?",
                "Confirm Payment",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String method =
                (String) paymentMethod.getSelectedItem();

        String transactionId =
                "TXN" + System.currentTimeMillis();

        Connection con = null;

        try {

            con = DatabaseConnection.getConnection();

            if (con == null) {

                JOptionPane.showMessageDialog(
                        this,
                        "Database connection failed."
                );

                return;
            }

            con.setAutoCommit(false);

            // Check and decrease available seats
            String seatSql =
                    "UPDATE events "
                    + "SET available_seats = available_seats - 1 "
                    + "WHERE event_id = ? AND available_seats > 0";

            try (PreparedStatement seatPs =
                    con.prepareStatement(seatSql)) {

                seatPs.setInt(1, eventId);

                if (seatPs.executeUpdate() != 1) {

                    throw new SQLException(
                            "Sorry, no seats are available."
                    );
                }
            }

            // Save payment details
            String paymentSql =
                    "INSERT INTO payments "
                    + "(booking_id, amount, payment_method, "
                    + "transaction_id, payment_status) "
                    + "VALUES (?, ?, ?, ?, 'Success')";

            try (PreparedStatement ps =
                    con.prepareStatement(paymentSql)) {

                ps.setInt(1, bookingId);
                ps.setDouble(2, amount);
                ps.setString(3, method);
                ps.setString(4, transactionId);

                ps.executeUpdate();
            }

            // Confirm booking
            String bookingSql =
                    "UPDATE bookings "
                    + "SET booking_status = 'Booked' "
                    + "WHERE booking_id = ? "
                    + "AND booking_status = 'Pending'";

            try (PreparedStatement ps =
                    con.prepareStatement(bookingSql)) {

                ps.setInt(1, bookingId);

                if (ps.executeUpdate() != 1) {

                    throw new SQLException(
                            "Booking is not pending."
                    );
                }
            }

            // Complete all database operations
            con.commit();

            JOptionPane.showMessageDialog(
                    this,
                    "PAYMENT SUCCESSFUL!\n\n"
                    + "Amount: Rs. " + amount
                    + "\nPayment Method: " + method
                    + "\nTransaction ID: " + transactionId
                    + "\n\nYour booking is confirmed!"
            );

            dispose();

        } catch (Exception ex) {

            try {

                if (con != null) {
                    con.rollback();
                }

            } catch (SQLException rollbackError) {

                rollbackError.printStackTrace();
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Payment Failed:\n" + ex.getMessage()
            );

        } finally {

            try {

                if (con != null) {

                    con.setAutoCommit(true);
                    con.close();
                }

            } catch (SQLException ex) {

                ex.printStackTrace();
            }
        }
    }

    // Cancel pending booking
    private void cancelPayment() {

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to cancel payment?",
                "Cancel Payment",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        String sql =
                "DELETE FROM bookings "
                + "WHERE booking_id = ? "
                + "AND booking_status = 'Pending'";

        try (Connection con = DatabaseConnection.getConnection()) {

            if (con == null) {

                JOptionPane.showMessageDialog(
                        this,
                        "Database connection failed."
                );

                return;
            }

            try (PreparedStatement ps =
                    con.prepareStatement(sql)) {

                ps.setInt(1, bookingId);

                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Payment cancelled."
            );

            dispose();

        } catch (SQLException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Could not cancel payment:\n" + ex.getMessage()
            );
        }
    }
}